package application

import akka.util.ByteString
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import scalatags.Text.all.Tag

trait ScalaTagsSupport:
  implicit def contentTypeOfTag: ContentTypeOf[Tag] =
    ContentTypeOf[Tag](Some(ContentTypes.HTML))

  implicit def writeableOfTag: Writeable[Tag] =
    Writeable(tag => ByteString("<!DOCTYPE html>\n" + tag.render))

object ScalaTagsSupport extends ScalaTagsSupport
