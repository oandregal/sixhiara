package es.icarto.gvsig.sixhiara.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.SaveFileDialog;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.sixhiara.plots.MaxValues.MaxValue;

@SuppressWarnings("serial")
public class AnalyticsChartPanel extends AbstractIWindow {

	private static final Dimension CHART_SIZE = new java.awt.Dimension(800, 600);

	private static final Logger logger = LoggerFactory
			.getLogger(AnalyticsChartPanel.class);

	private final Field field;
	private final Map<String, Number[]> data;
	private final int firstYear;
	private final int currentYear;
	private final JFreeChart chart;

	private XYSeries maxSeries = null;

	private XYSeries minSeries;

	public AnalyticsChartPanel(Map<String, Number[]> selectedFontes,
			int firstYear, int currentYear, Field field) {
		super();
		this.data = selectedFontes;
		this.firstYear = firstYear;
		this.currentYear = currentYear;
		this.field = field;
		final XYDataset dataset = createDataset();
		chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart, true, true, true,
				true, true);
		chartPanel.setPreferredSize(CHART_SIZE);
		add(chartPanel);
		initToolbar();
	}

	private void initToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel label = new JLabel("Gravar em formato: ");
		toolbar.add(label);
		JButton pngBt = new JButton("PNG");
		pngBt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SaveFileDialog dialog = new SaveFileDialog("Ficheiros PNG",
						"png");
				File file = dialog.showDialog();
				if (file == null) {
					return;
				}

				try {
					ChartUtilities.saveChartAsPNG(file, chart,
							CHART_SIZE.width, CHART_SIZE.height);
				} catch (IOException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
		});
		JButton xlsBt = new JButton("XLS");
		xlsBt.addActionListener(new ExportToXLSActionListener(data, firstYear,
				currentYear, field));
		toolbar.add(pngBt);
		toolbar.add(xlsBt);
		this.add(toolbar, "dock south");
	}

	public void load() {
	}

	@Override
	protected JButton getDefaultButton() {
		return null;
	}

	@Override
	protected Component getDefaultFocusComponent() {
		return null;
	}

	private XYDataset createDataset() {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		Integer[] years = getYears();
		initMaxMin(dataset, years);
		for (String key : data.keySet()) {
			final XYSeries series = new XYSeries(key);
			for (int i = 0; i < years.length; i++) {
				if ((data.get(key)[i]) != null) {
					series.add(years[i], data.get(key)[i]);
				}
			}
			dataset.addSeries(series);
		}
		return dataset;
	}

	private void initMaxMin(XYSeriesCollection dataset, Integer[] years) {
		MaxValue maxValue = new MaxValues().getMaxFor(field.getKey());

		if (maxValue != null) {
			if (maxValue.max != null) {
				maxSeries = new XYSeries("M�ximo");
				for (int i = 0; i < years.length; i++) {
					maxSeries.add(years[i], maxValue.max);
				}
				dataset.addSeries(maxSeries);
			}
			if (maxValue.min != null) {
				minSeries = new XYSeries("M�nimo");
				for (int i = 0; i < years.length; i++) {
					minSeries.add(years[i], maxValue.min);
				}
				dataset.addSeries(minSeries);
			}
		}

	}

	private Integer[] getYears() {
		Number[] next = data.values().iterator().next();
		Integer[] years = new Integer[next.length];

		Integer year = Integer.valueOf(firstYear);
		for (int i = 0; i < years.length; i++) {
			years[i] = year;
			year++;
		}
		return years;
	}

	private JFreeChart createChart(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				field.getLongName(), // chart title
				"Ano", // x axis label
				field.getLongName(), // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// final StandardLegend legend = (StandardLegend) chart.getLegend();
		// legend.setDisplaySeriesShapes(true);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);

		if (maxSeries != null) {
			renderer.setSeriesShapesVisible(0, false);
			renderer.setSeriesStroke(0, new BasicStroke(3));
			renderer.setSeriesItemLabelsVisible(0, false);
		}
		if (minSeries != null) {
			renderer.setSeriesShapesVisible(1, false);
			renderer.setSeriesStroke(1, new BasicStroke(3));
			renderer.setSeriesItemLabelsVisible(1, false);
		}

		plot.setRenderer(renderer);
		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		initXAxis(plot);
		// splitLegends(plot);
		// addLabelsToPoints(plot);

		return chart;
	}

	// private void addLabelsToPoints(XYPlot plot) {
	// plot.renderer.baseItemLabelGenerator =
	// plot.renderer.baseItemLabelsVisible = true
	// }

	/**
	 * X Axis shows only integers without miles separator
	 */
	private void initXAxis(XYPlot plot) {
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		domainAxis.setNumberFormatOverride(format);
	}

	private void splitLegends(XYPlot plot) {
		LegendTitle legend1 = new LegendTitle(plot.getRenderer(0));
		legend1.setMargin(new RectangleInsets(2, 2, 2, 2));
		legend1.setFrame(new BlockBorder());

		LegendTitle legend2 = new LegendTitle(plot.getRenderer(1));
		legend2.setMargin(new RectangleInsets(2, 2, 2, 2));
		legend2.setFrame(new BlockBorder());

		BlockContainer container = new BlockContainer(new BorderArrangement());
		container.add(legend1, RectangleEdge.LEFT);
		container.add(legend2, RectangleEdge.RIGHT);
		container.add(new EmptyBlock(2000, 0));
		CompositeTitle legends = new CompositeTitle(container);
		legends.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legends);

		ChartUtilities.applyCurrentTheme(chart);
	}

}
