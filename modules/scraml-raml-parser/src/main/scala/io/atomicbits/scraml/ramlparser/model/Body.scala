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

package io.atomicbits.scraml.ramlparser.model

import play.api.libs.json.{JsObject, JsValue}
import io.atomicbits.scraml.ramlparser.parser.TryUtils._

import scala.language.postfixOps
import scala.util.{Success, Try}


/**
  * Created by peter on 26/08/16.
  */
case class Body(headerMap: Map[Header, MimeType] = Map.empty) {

  def forHeader(header: Header): Option[MimeType] = headerMap.get(header)

}


object Body {

  def apply(json: JsValue): Try[Body] = {

    def fromJsObject(jsObj: JsObject): Try[Body] = {

      val tryMimeTypes =
        jsObj.value.collect {
          case MimeType(tryMimeType) => tryMimeType
        } toSeq

      val tryHeaderMap: Try[Map[Header, MimeType]] =
        accumulate(tryMimeTypes).map { mimeTypes =>
          mimeTypes.map { mType =>
            mType.header -> mType
          } toMap
        }

      tryHeaderMap.map { headerMap =>
        Body(headerMap)
      }
    }

    json \ "body" toOption match {
      case Some(jsObj: JsObject) => fromJsObject(jsObj)
      case _                     => Success(Body())
    }
  }

}
