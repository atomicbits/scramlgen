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

package io.atomicbits.scraml.ramlparser.lookup

import org.slf4j.{ Logger, LoggerFactory }
import io.atomicbits.scraml.ramlparser.lookup.transformers._
import io.atomicbits.scraml.ramlparser.model.canonicaltypes._
import io.atomicbits.scraml.ramlparser.model.parsedtypes._

/**
  * Created by peter on 17/12/16.
  */
object ParsedToCanonicalTypeTransformer {

  val LOGGER: Logger = LoggerFactory.getLogger("ParsedToCanonicalTypeTransformer")

  /**
    * Transform the given parsed type to a canonical type, recursively parsing and registering all internal parsed types .
    *
    * @param parsed                The parsed type to transform.
    * @param canonicalLookupHelper This canonical lookup helper registers all internal parsed types that we see while processing the
    *                              given parsed type.
    * @return
    */
  // format: off
  def transform(parsed: ParsedType,
                canonicalLookupHelper: CanonicalLookupHelper,
                canonicalName: Option[CanonicalName] = None)
               (implicit canonicalNameGenerator: CanonicalNameGenerator): (GenericReferrable, CanonicalLookupHelper) = { // format: on

    ParsedTypeContext(parsed, canonicalLookupHelper, canonicalName) match {
      case ParsedObjectTransformer(typeReference, updatedLookupHelper)        => (typeReference, updatedLookupHelper)
      case ParsedTypeReferenceTransformer(typeReference, updatedLookupHelper) => (typeReference, updatedLookupHelper)
      case ParsedEnumTransformer(typeReference, updatedLookupHelper)          => (typeReference, updatedLookupHelper)
      case ParsedArrayTransformer(typeReference, updatedLookupHelper)         => (typeReference, updatedLookupHelper)
      case ParsedBooleanTransformer(typeReference, updatedLookupHelper)       => (typeReference, updatedLookupHelper)
      case ParsedStringTransformer(typeReference, updatedLookupHelper)        => (typeReference, updatedLookupHelper)
      case ParsedNumberTransformer(typeReference, updatedLookupHelper)        => (typeReference, updatedLookupHelper)
      case ParsedIntegerTransformer(typeReference, updatedLookupHelper)       => (typeReference, updatedLookupHelper)
      case ParsedNullTransformer(typeReference, updatedLookupHelper)          => (typeReference, updatedLookupHelper)
      case ParsedGenericObjectTransformer(typeReference, updatedLookupHelper) => (typeReference, updatedLookupHelper)
      case ParsedTypeContext(fragments: Fragments, _, _, _, _) =>
        LOGGER.info(s"Skipped unknown json-schema fragments.")
        (NullType, canonicalLookupHelper)
      case x => sys.error(s"Error transforming $x")
      // Currently not yet supported:
      //      case parsedFile: ParsedFile                               => ???
      //      case parsedNull: ParsedNull                               => ???
      //      case parsedUnionType: ParsedUnionType                     => ???
      //      case parsedDateOnly: ParsedDateOnly                       => ???
      //      case parsedTimeOnly: ParsedTimeOnly                       => ???
      //      case parsedDateTimeOnly: ParsedDateTimeOnly               => ???
      //      case parsedDateTimeDefault: ParsedDateTimeDefault         => ???
      //      case parsedDateTimeRFC2616: ParsedDateTimeRFC2616         => ???
    }

  }

}
