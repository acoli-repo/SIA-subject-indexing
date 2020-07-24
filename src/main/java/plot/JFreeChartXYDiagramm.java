package plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Draw xy diagramm with one ore more function plots
 * @author frank
 *
 */
public class JFreeChartXYDiagramm {
	
	public String title = "";
	public NumberAxis xax;
	public NumberAxis yax;
	public XYSeriesCollection datasets = new XYSeriesCollection();
	
	/**
	 * Draw XY-diagramm with one ore more functions
	 * @param data Series of data
	 * @param xaxisLabel Label x-axis
	 * @param yaxisLabel Label y-axis
	 * @param title Frame title
	 * @param tickUnitX Number of ticks on x-axis
	 * @param tickUnitY Number of ticks on y-axis
	 * @param rangeXLow min x-axis value
	 * @param rangeXHigh max x-axis value
	 * @param rangeYLow min y-axis value
	 * @param rangeYHigh max y-axis value
	 */
	public JFreeChartXYDiagramm (
			List<XYSeries> data,
			String xaxisLabel,
			String yaxisLabel,
			String title,
			double tickUnitX,
			double tickUnitY,
			double rangeXLow,
			double rangeXHigh,
			double rangeYLow,
			double rangeYHigh
			) {
		
		xax = new NumberAxis(xaxisLabel);
		xax.setTickUnit(new NumberTickUnit(tickUnitX));
		xax.setRange(rangeXLow, rangeXHigh);
		yax = new NumberAxis(yaxisLabel);
		yax.setTickUnit(new NumberTickUnit(tickUnitY));
		yax.setRange(rangeYLow, rangeYHigh);

		this.title = title;
		
		for (XYSeries x : data) {
			datasets.addSeries(x);
		}
		
		draw();
	}

	/**
	 * 
	 */
	private void draw() {
		
		ApplicationFrame frame = new ApplicationFrame("");
        frame.setTitle(this.title);
        
        XYSplineRenderer spline = new XYSplineRenderer();
        spline.setPrecision(1);
        System.out.println("spline precision = "+(spline.getPrecision()));
        
        XYPlot plot = new XYPlot(this.datasets,xax,yax, spline);
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel chartPanel = new ChartPanel(chart);
        
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
		
	}
	
	public static void main (String[] args) {
		
		XYSeries z = new XYSeries("data-1");
		z.add(1, 0.2857143);
		z.add(2,0.41239893);
		z.add(3,0.4690027);
		z.add(4,0.509434);
		z.add(5,0.53908354);
		
		XYSeries z2 = new XYSeries("data-2");
		z2.add(1, 0.8857143);
		z2.add(2,0.81239893);
		z2.add(3,0.4690027);
		z2.add(4,0.709434);
		z2.add(5,0.93908354);
		
		ArrayList<XYSeries> data = new ArrayList <XYSeries>();
		data.add(z);
		data.add(z2);
		
		new JFreeChartXYDiagramm(
				data,
				"Best k computed keywords",
				"Precision (matched manually assigned keywords)",
				"Match computed keywords - Test document set=371 (20%), KeywordMinDocSupport=50",
				1.0,
				0.05,
				0,
				7.0,
				0,
				1.0);
	}

}
