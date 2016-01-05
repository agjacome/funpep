package funpep

import java.nio.file.Path

import scala.collection.JavaConverters._

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.string._
import scalaz.syntax.std.option._

import util.functions._
import util.ops.path._


// TODO: Do not hardcode configuration (colors, graphicsType, image size and
// type [now always PNG]), use a Reader/Kleisli with a new Config object
// holding those options. Something like:
//
//   final case class TreeConfig (
//     colors:    TreeColorSet ==>> Color,
//     graphType: PHYLOGENY_GRAPHICS_TYPE,
//     imageSize: (Int, Int),
//     imageType: AptxUtil.GraphicsExportType
//   )
//
//   def generateTreeFiles(...): KleisliP[TreeConfig, Unit] = ???
//
// It also would be nice to define at least type aliases for that ugly naming
// style that Forester uses.
object TreeReporter {

  def generateTreeFiles(newick: Path, annotations: String ==>> String)(phyloxml: Path, png: Path): Process[Task, Unit] = {
    import java.awt.Color
    import org.forester.archaeopteryx._
    import org.forester.io.parsers.nhx.NHXParser
    import org.forester.io.writers.PhylogenyWriter
    import org.forester.phylogeny._
    import org.forester.util.ForesterUtil

    // Effectful methods of Forester
    def configuration: Configuration = {
      val conf = new Configuration

      conf.putDisplayColors(TreeColorSet.BACKGROUND, Color.WHITE)
      conf.putDisplayColors(TreeColorSet.BRANCH,     Color.BLACK)
      conf.putDisplayColors(TreeColorSet.TAXONOMY,   Color.BLACK)
      conf.putDisplayColors(TreeColorSet.ANNOTATION, Color.BLACK)
      conf.putDisplayColors(TreeColorSet.SEQUENCE,   Color.BLACK)
      conf.setPhylogenyGraphicsType(Options.PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR)

      conf
    }

    def readPhylogeny: Phylogeny = {
      val phy = PhylogenyMethods.readPhylogenies(new NHXParser, newick.absolute.toFile).head

      phy.iteratorPostorder.asScala foreach { node ⇒
        val name       = node.getName
        val annotation = annotations.lookup(name) | name
        node.setName(annotation)
      }

      phy
    }

    def writePhyloXML(phy: Phylogeny): Unit = {
      val writer = new PhylogenyWriter
      writer.toPhyloXML(Array(phy), 0, phyloxml.absolute.toFile, ForesterUtil.LINE_SEPARATOR)
    }

    def writePNGImage(phy: Phylogeny, conf: Configuration): Unit = {
      AptxUtil.writePhylogenyToGraphicsFile(
        phy, png.absolute.toFile, 1000, 1000, AptxUtil.GraphicsExportType.PNG, conf
      )
    }

    // Forester side-effect hiding inside a scalaz-stream Process
    AsyncP(readPhylogeny) map { phy ⇒
      writePhyloXML(phy)
      writePNGImage(phy, configuration)
    }
  }

}
