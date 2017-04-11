/*
 *
 *  (C) Copyright 2015 Atomic BITS (http://atomicbits.io).
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the GNU Affero General Public License
 *  (AGPL) version 3.0 which accompanies this distribution, and is available in
 *  the LICENSE file or at http://www.gnu.org/licenses/agpl-3.0.en.html
 *  Alternatively, you may also use this code under the terms of the
 *  Scraml Commercial License, see http://scraml.io
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License or the Scraml Commercial License for more
 *  details.
 *
 *  Contributors:
 *      Peter Rigole
 *
 */

package io.atomicbits.scraml.generator.platform.scalaplay

import io.atomicbits.scraml.generator.codegen.GenerationAggr
import io.atomicbits.scraml.generator.platform.{ Platform, SourceGenerator }
import io.atomicbits.scraml.generator.typemodel.{ HeaderSegmentClassDefinition, SourceFile }

/**
  * Created by peter on 18/01/17.
  */
object HeaderSegmentClassGenerator extends ScalaPlaySourceGenerator {

  import Platform._

  implicit val platform: Platform = ScalaPlay

  def generate(generationAggr: GenerationAggr, headerSegmentClassDefinition: HeaderSegmentClassDefinition): GenerationAggr = {

    val className   = headerSegmentClassDefinition.reference.name
    val packageName = headerSegmentClassDefinition.reference.packageName
    val imports     = platform.importStatements(headerSegmentClassDefinition.reference, headerSegmentClassDefinition.imports)
    val methods     = headerSegmentClassDefinition.methods

    val source =
      s"""
         package $packageName

         import $dslBasePackageString._
         import play.api.libs.json._
         import java.io._

         ${imports.mkString("\n")}


         class $className(_req: RequestBuilder) extends HeaderSegment(_req) {

           ${methods.mkString("\n")}

         }
       """

    val filePath = headerSegmentClassDefinition.reference.toFilePath

    generationAggr.addSourceFile(SourceFile(filePath = filePath, content = source))
  }

}
