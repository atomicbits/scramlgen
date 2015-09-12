/*
 *
 *  (C) Copyright 2015 Atomic BITS (http://atomicbits.io).
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the GNU Affero General Public License
 *  (AGPL) version 3.0 which accompanies this distribution, and is available in
 *  the LICENSE file or at http://www.gnu.org/licenses/agpl-3.0.en.html
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *
 *  Contributors:
 *      Peter Rigole
 *
 */

package io.atomicbits.scraml.generator.codegen.scala

import io.atomicbits.scraml.generator.model._
import io.atomicbits.scraml.parser.model._

import scala.language.postfixOps

/**
 * Created by peter on 23/08/15. 
 */
object ActionGenerator {

  def generateActionImports(action: RichAction): Set[String] = {

    def nonPredefinedImports(classReps: List[ClassRep]): Set[String] = {
      classReps match {
        case cr :: crs if !cr.predef => nonPredefinedImports(cr.types) ++ nonPredefinedImports(crs) + s"import ${cr.fullyQualifiedName}"
        case cr :: crs               => nonPredefinedImports(cr.types) ++ nonPredefinedImports(crs)
        case Nil                     => Set()
      }
    }

    val contentTypeImports =
      action.contentTypes.collect {
        case TypedContentType(contentTypeHeader, classRep) => nonPredefinedImports(List(classRep))
      }.flatten

    val responseTypeImports =
      action.responseTypes.collect {
        case TypedResponseType(acceptHeader, classRep) => nonPredefinedImports(List(classRep))
      }.flatten

    contentTypeImports ++ responseTypeImports
  }

  /**
   * The reason why we treat all actions of a resource together is that certain paths towards the actual action
   * execution of the resource's actions may be overlapping when it concerns actions that have overlapping mandatory
   * content-type and/or accept header paths. Although such situations may be rare, we want to support them (in the future),
   * so we pass all actions of a single resource together.
   *
   * @param resource The resource whose actions are going to be processed (NOT recursively!)
   * @return A list of action function definitions or action paths that lead to the action function. Action paths will only be
   *         required if multiple contenttype and/or accept headers will lead to a different typed body and/or response (we
   *         don't support those yet, but we will do so in the future).
   */
  def generateActionFunctions(resource: RichResource): List[String] = {

    val actions: List[RichAction] = resource.actions

    val actionsWithSafeContentAndResponseTypes =
      actions map {
        case action if action.contentTypes.isEmpty => action.copy(contentTypes = Set(NoContentType))
        case action                                => action
      } map {
        case action if action.responseTypes.isEmpty => action.copy(responseTypes = Set(NoResponseType))
        case action                                 => action
      }

    val actionsWithTypeSelection: List[RichAction] =
      actionsWithSafeContentAndResponseTypes.flatMap { action =>
        for {
          contentType <- action.contentTypes
          responseType <- action.responseTypes
          actionWithTypeSelection = action.copy(selectedContentType = contentType, selectedResponsetype = responseType)
        } yield actionWithTypeSelection
      }

    val groupedByActionType: Map[ActionType, List[RichAction]] = actionsWithTypeSelection.groupBy(_.actionType)

    // now, we have to map the actions onto a segment path if necessary
    val actionPathToAction: List[ActionPath] =
      groupedByActionType.values flatMap {
        case actionOfKindList@(aok :: Nil)  => List(ActionPath(NoContentHeaderSegment, NoAcceptHeaderSegment, actionOfKindList.head))
        case actionOfKindList@(aok :: aoks) =>
          actionOfKindList map { actionOfKind =>
            val contentHeader =
              actionOfKind.selectedContentType match {
                case NoContentType   => NoContentHeaderSegment
                case ct: ContentType => ActualContentHeaderSegment(ct)
              }
            val acceptHeader =
              actionOfKind.selectedResponsetype match {
                case NoResponseType   => NoAcceptHeaderSegment
                case rt: ResponseType => ActualAcceptHeaderSegment(rt)
              }
            ActionPath(contentHeader, acceptHeader, actionOfKind)
          }
      } toList

    val uniqueActionPaths: Map[ContentHeaderSegment, Map[AcceptHeaderSegment, List[RichAction]]] =
      actionPathToAction
        .groupBy(_.contentHeader)
        .mapValues(_.groupBy(_.acceptHeader))
        .mapValues(_.mapValues(_.map(_.action)))

    uniqueActionPaths

    groupedByActionType.values.flatten.flatMap(ActionFunctionGenerator.generate).toList

  }

  case class ActionPath(contentHeader: ContentHeaderSegment, acceptHeader: AcceptHeaderSegment, action: RichAction)

  sealed trait HeaderSegment

  trait ContentHeaderSegment extends HeaderSegment

  trait AcceptHeaderSegment extends HeaderSegment

  case object NoContentHeaderSegment extends ContentHeaderSegment

  case class ActualContentHeaderSegment(header: ContentType) extends ContentHeaderSegment

  case object NoAcceptHeaderSegment extends AcceptHeaderSegment

  case class ActualAcceptHeaderSegment(header: ResponseType) extends AcceptHeaderSegment

}
