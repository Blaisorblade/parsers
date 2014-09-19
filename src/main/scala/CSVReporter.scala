import java.io.{File,PrintWriter}
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import org.scalameter.utils.Tree
import org.scalameter.{CurveData, Key, Persistor, Reporter}

case class CSVReporter() extends Reporter {
  override def report(result: CurveData, persistor: Persistor): Unit = {}

  override def report(results: Tree[CurveData], persistor: Persistor): Boolean = {
    val dir: String = results.context.goe(Key.reports.resultDir, "tmp")
    val fileName = s"${dateISO(new Date())}.csv"

    new File(dir).mkdirs()
    val writer = new PrintWriter(s"$dir/$fileName")

    results foreach writeToFile(writer)

    if (writer.checkError()) throw new Exception("Stuff went wrong")

    writer.close()
    true
  }

  // from org.scalameter.reporting.DsvReporter
  val dateISO: (Date => String) = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    (date) => df.format(date)
  }

  def writeToFile(writer: PrintWriter)(result: CurveData) = {
    val nameInner = result.context(Key.dsl.scope)(0)
    val nameOuter = result.context(Key.dsl.scope)(1)

    result.measurements.foreach(measurement => {
      val params = measurement.params
      measurement.data.complete foreach(datapoint => {
        writer.format("%s;%s;%s;%e%n", nameOuter, nameInner, params.axisData.values.head.toString, datapoint: java.lang.Double)
      })
    })
  }
}
