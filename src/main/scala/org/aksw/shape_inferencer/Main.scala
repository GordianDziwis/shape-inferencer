package org.aksw.shape_inferencer

import net.sansa_stack.rdf.spark.io._
import net.sansa_stack.rdf.spark.model.TripleOperations
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.riot.Lang
import org.apache.spark.sql.SparkSession
import scala.collection.mutable.HashMap

object Main {

  def main(args: Array[String]): Unit =
    parser.parse(args, Config()) match {
      case Some(config) =>
        run(config.in)
      case None =>
        println(parser.usage)
    }

  def getType(typeMap: HashMap[Node, Node], node: Node): Node =
    node.isLiteral match {
      case true => NodeFactory.createURI(node.getLiteralDatatypeURI)
      case false =>
        typeMap getOrElse (node, NodeFactory.createURI("NO TYPE"))
    }

  def run(input: String): Unit = {

    val spark = SparkSession.builder
      .appName(s"Shape Inferencer  $input")
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    val lang = Lang.NTRIPLES
    val triples = spark.rdf(lang)(input)
    val typeTriple =
      NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    val typeTriples = triples.find(None, Some(typeTriple), None).collect()
    val typeMap = typeTriples.foldLeft(HashMap[Node, Node]()) { (map, triple) =>
      map += (triple.getSubject -> triple.getObject)
    }
    // (Key, Value) = ((Type, Predicate, Object), 1)
    val instanceTriples = triples
      .map(
        triple =>
          ((getType(typeMap, triple.getSubject),
            triple.getPredicate,
            getType(typeMap, triple.getMatchObject)),
           1L)
      )
      .reduceByKey(_ + _)
    val sortedTriples = instanceTriples
      .sortBy(
        tuple => (tuple._1._1.getURI, tuple._1._2.getURI, tuple._1._3.getURI),
        true
      )
      .collect
    sortedTriples.foreach(println)
    spark.stop
  }

  case class Config(in: String = "")

  val parser = new scopt.OptionParser[Config]("Shape Inferencer") {
    head("Shape Inferencer")
    opt[String]('i', "input")
      .required()
      .valueName("<path>")
      .action((x, c) => c.copy(in = x))
      .text("path to file that contains the data (in N-Triples format)")
  }
}
