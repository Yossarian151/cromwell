package centaur.cwl

import centaur.api.CentaurCromwellClient
import cromwell.api.model.SubmittedWorkflow
import cwl.{CwlDecoder, MyriadOutputType}
import io.circe.syntax._
import shapeless.Poly1
import spray.json.{JsObject, JsString, JsValue}

import scalaz.syntax.std.map._

object Outputs {

  //When the string returned is not valid JSON, it is effectively an exception as CWL runner expects JSON to be returned
  def handleOutput(submittedWorkflow: SubmittedWorkflow): String = {
    val metadata: Map[String, JsValue] = CentaurCromwellClient.metadata(submittedWorkflow).get.value

    //Sorry for all the nesting, but spray json JsValue doesn't have optional access methods like Argonaut/circe,
    //thus no for comprehensions for us :(
    metadata.get("submittedFiles.workflow") match {
      case Some(JsString(workflow)) =>

        val cwl = CwlDecoder.decodeTopLevelCwl(workflow)

        cwl.value.attempt.unsafeRunSync() match {
          case Right(Right(cwl)) =>

            CentaurCromwellClient.outputs(submittedWorkflow).get.outputs match {
              case JsObject(map) =>
                val typeMap: Map[String, MyriadOutputType] = cwl.fold(CwlOutputsFold)
                val mungeTypeMap = typeMap.mapKeys(stripTypeMapKey)

                val mungeOutputMap = map.mapKeys(stripOutputKey)

                mungeOutputMap.
                  //This lets us operate on the values of the output values and types for a particular output key
                  intersectWith(mungeTypeMap)(OutputManipulator.resolveOutput).
                  //converting the whole response to Json using Circe's auto-encoder derivation
                  asJson.
                  //drop null values so that we don't print when Option == None
                  pretty(io.circe.Printer.spaces2.copy(dropNullValues = true))
              case other => s"it seems cromwell is not returning outputs as a Jsobject but is instead a $other"
            }
          case Right(Left(error)) => s"couldn't parse workflow: $workflow failed with error: $error"
          case Left(error) => s"Exception when trying to read workflow: $workflow failed with error: $error"
        }
      case Some(other) => s"received the value $other when the workflow string was expected"
      case None => "the workflow is no longer in the metadata payload, it's a problem"
    }
  }

  //Ids come out of SALAD pre-processing with a filename prepended.  This gets rid of it
  def stripTypeMapKey(key: String): String = key.substring(key.lastIndexOf("#") + 1, key.length)

  //Ids come out of Cromwell with a prefix, separated by a ".".  This takes everything to the right,
  //as CWL wants it
  def stripOutputKey(key: String): String = key.substring(key.lastIndexOf(".") + 1, key.length)

}

object CwlOutputsFold extends Poly1 {
  import cwl._

  implicit def wf: Case.Aux[cwl.Workflow, Map[String, MyriadOutputType]] = at[cwl.Workflow] {
    _.outputs.map(output => output.id -> output.`type`.get).toMap
  }

  implicit def clt: Case.Aux[cwl.CommandLineTool, Map[String, MyriadOutputType]] = at[cwl.CommandLineTool] {
    _.outputs.map(output => output.id -> output.`type`.get).toMap
  }

  implicit def et: Case.Aux[cwl.ExpressionTool, Map[String, MyriadOutputType]] = at[cwl.ExpressionTool] {
    _.outputs.map(output => output.id -> output.`type`).toMap
  }
}

