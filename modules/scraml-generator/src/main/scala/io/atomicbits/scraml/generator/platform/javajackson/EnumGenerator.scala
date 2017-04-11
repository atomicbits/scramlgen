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

package io.atomicbits.scraml.generator.platform.javajackson

import io.atomicbits.scraml.generator.codegen.GenerationAggr
import io.atomicbits.scraml.generator.platform.{ Platform, SourceGenerator }
import io.atomicbits.scraml.generator.typemodel.{ EnumDefinition, SourceFile }
import io.atomicbits.scraml.generator.util.CleanNameUtil
import io.atomicbits.scraml.generator.platform.Platform._

/**
  * Created by peter on 1/03/17.
  */
object EnumGenerator extends JavaJacksonSourceGenerator {

  implicit val platform: Platform = JavaJackson

  def generate(generationAggr: GenerationAggr, enumDefinition: EnumDefinition): GenerationAggr = {

    // Accompany the enum names with their 'Java-safe' name.
    val enumsWithSafeName =
      enumDefinition.values map { value =>
        val safeName = CleanNameUtil.escapeJavaKeyword(CleanNameUtil.cleanEnumName(value))
        s"""$safeName("$value")""" // e.g. space("spa ce")
      }

    val classNameCamel = CleanNameUtil.camelCased(enumDefinition.reference.name)

    val source =
      s"""
        package ${enumDefinition.reference.packageName};

        import com.fasterxml.jackson.annotation.*;

        public enum ${enumDefinition.reference.name} {

          ${enumsWithSafeName.mkString("", ",\n", ";\n")}

          private final String value;

          private ${enumDefinition.reference.name}(final String value) {
                  this.value = value;
          }

          @JsonValue
          final String value() {
            return this.value;
          }

          @JsonCreator
          public static ${enumDefinition.reference.name} fromValue(String value) {
            for (${enumDefinition.reference.name} $classNameCamel : ${enumDefinition.reference.name}.values()) {
              if (value.equals($classNameCamel.value())) {
                return $classNameCamel;
              }
            }
            throw new IllegalArgumentException("Cannot instantiate a ${enumDefinition.reference.name} enum element from " + value);
          }

        }
       """

    val sourceFile =
      SourceFile(
        filePath = enumDefinition.reference.toFilePath,
        content  = source
      )

    generationAggr.copy(sourceFilesGenerated = sourceFile +: generationAggr.sourceFilesGenerated)
  }

}
