package centaur.test.metadata

import java.util.UUID

import cats.data.Validated._
import com.typesafe.config.Config
import configs.syntax._
import spray.json._
import centaur.json.JsonUtils.EnhancedJsValue
import configs.Result
import common.validation.ErrorOr.ErrorOr

import scala.util.{Failure, Success, Try}

case class WorkflowMetadata(value: Map[String, JsValue]) extends AnyVal {

  def diff(actual: WorkflowMetadata, workflowID: UUID, cacheHitUUID: Option[UUID] = None): Iterable[String] = {
    // If the test fails in initialization there wouldn't be workflow root metadata, and if that's the expectation
    // then that's ok.
    val workflowRoot = actual.value.get("workflowRoot").collectFirst { case JsString(r) => r } getOrElse "No Workflow Root"
    val missingErrors = value.keySet.diff(actual.value.keySet) map { k => s"Missing key: $k" }
    val mismatchErrors = value.keySet.intersect(actual.value.keySet) flatMap { k => diffValues(k, value(k), actual.value(k),
                                                                                               workflowID, workflowRoot, cacheHitUUID)}

    mismatchErrors ++ missingErrors
  }

  private def diffValues(key: String, expected: JsValue, actual: JsValue, workflowID: UUID, workflowRoot: String, cacheHitUUID: Option[UUID]): Option[String] = {
    /*
      FIXME/TODO:

      At the moment all expected values are coerced into JsString (as it is hard to discern intended type from
      HOCON). However values coming from the metadata endpoint are JsValue. At the moment we're only using
      JsString, JsNumber and JsBoolean for comparison so this is a hacky way of handling that situation. It's
      entirely likely that it won't survive long term.
     */

    lazy val substitutedValue = expected.toString.replace("<<UUID>>", workflowID.toString).replace("<<WORKFLOW_ROOT>>", workflowRoot)
    lazy val cacheSubstitutions = cacheHitUUID match {
      case Some(uuid) => substitutedValue.replace("<<CACHE_HIT_UUID>>", uuid.toString)
      case None => substitutedValue
    }

    val isMatch = actual match {
      case o: JsString => cacheSubstitutions == o.toString
      case o: JsNumber => expected == JsString(o.value.toString)
      case o: JsBoolean => expected == JsString(o.value.toString)
      case o: JsArray => expected == JsString(o.toString)
      case JsNull => expected == JsNull
      case _ => false
    }

    if (isMatch) None
    else Option(s"Metadata mismatch for $key - expected: $cacheSubstitutions but got: $actual")
  }
}

object WorkflowMetadata {
  def fromConfig(config: Config): ErrorOr[WorkflowMetadata] = {
    config.extract[Map[String, Option[String]]] match {
      case Result.Success(m) => Valid(WorkflowMetadata(m mapValues { _.map(JsString.apply).getOrElse(JsNull) }))
      case Result.Failure(_) => invalidNel(s"Metadata block can not be converted to a Map: $config")
    }
  }

  def fromMetadataJson(json: String): ErrorOr[WorkflowMetadata] = {
    import DefaultJsonProtocol._

    Try(json.parseJson.asJsObject.flatten().convertTo[Map[String, JsValue]]) match {
      case Success(m) => Valid(WorkflowMetadata(m))
      case Failure(e) => invalidNel(s"Unable to create Metadata from JSON: ${e.getMessage}")
    }
  }
}
