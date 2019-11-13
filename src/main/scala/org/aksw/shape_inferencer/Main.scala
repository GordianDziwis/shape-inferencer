package org.aksw.shape_inferencer

import net.sansa_stack.rdf.spark.io._
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.riot.Lang
import org.apache.spark.sql.SparkSession
import scala.collection.mutable.HashMap
import scala.collection.immutable.SortedSet
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  implicit val myOrdering = Ordering.fromLessThan[Node](_.getURI > _.getURI)

  def main(args: Array[String]): Unit =
    parser.parse(args, Config()) match {
      case Some(config) =>
        run(config.in)
      case None =>
        println(parser.usage)
    }

  def getType(
      typeMap: HashMap[String, SortedSet[Node]],
      node: Node
  ): SortedSet[Node] =
    node.isLiteral match {
      case true => SortedSet(NodeFactory.createURI(node.getLiteralDatatypeURI))
      case false =>
        typeMap getOrElse (node.getURI, SortedSet(
          NodeFactory.createURI("NO TYPE")
        ))
    }

  def run(input: String): Unit = {
    val spark = SparkSession.builder
      .appName(s"Shape Inferencer  $input")
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    val triples = spark
      .rdf(Lang.NTRIPLES)(input)
      .filter(!_.getSubject.isBlank)
      .filter(!_.getObject.isBlank)
    logger.info(triples.count + s" triples")
    val typeNode =
      NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    val instanceToTypeMap = triples
      .filter(_.predicateMatches(typeNode))
      .collect()
      .foldLeft(HashMap[String, SortedSet[Node]]()) { (map, triple) =>
        val subjectUri = triple.getSubject.getURI
        map += (subjectUri ->
          (map.getOrElse(subjectUri, SortedSet()) + triple.getObject))
      }
    logger.info(instanceToTypeMap.size + s" instances")
    val instanceTypeSets = triples
      .filter(!_.predicateMatches(typeNode))
      // ((Set(Type Subject), Predicate, Set(Type Object), Subject), 1)
      .map(
        triple =>
          ((getType(instanceToTypeMap, triple.getSubject),
            triple.getPredicate,
            getType(instanceToTypeMap, triple.getObject),
            triple.getSubject),
           1L)
      )
    val typeCount = instanceTypeSets
      .map(tuple => (tuple._1._1, tuple._1._4))
      .distinct()
      .mapValues(_ => 1L)
      .reduceByKey(_ + _)
      .sortBy(
        tuple => (tuple._1.firstKey),
        true
      )
    val graphSummary = instanceTypeSets
      // ((Set(Type Subject), Predicate, Set(Type Object), Subject), card)
      .reduceByKey(_ + _)
      // ((Set(Type Subject), Predicate, Set(Type Object)), card -> 1)
      .map(
        tuple =>
          ((tuple._1._1, tuple._1._2, tuple._1._3),
           collection.immutable.HashMap[Long, Long](tuple._2 -> 1L))
      )
      // ((Set(Type Subject), Predicate, Set(Type Object)), HashMap(card -> n))
      .reduceByKey(
        (first, second) =>
          first.merged(second) {
            case ((k, v0), (_, v1)) => (k -> (v0 + v1))
          }
      )
      .sortBy(
        tuple =>
          (tuple._1._1.firstKey, tuple._1._2.getURI, tuple._1._3.firstKey),
        true
      )
    graphSummary.foreach(println)
    typeCount.foreach(println)
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
