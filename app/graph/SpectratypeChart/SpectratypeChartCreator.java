package graph.SpectratypeChart;

import com.antigenomics.vdjtools.basic.Spectratype;
import com.antigenomics.vdjtools.sample.Clonotype;
import com.antigenomics.vdjtools.sample.Sample;
import models.UserFile;
import play.Logger;
import play.libs.Json;
import utils.CacheType.CacheType;
import utils.server.LogAggregator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class SpectratypeChartCreator {
    private UserFile file;
    private Spectratype sp;
    private List<Clonotype> topclones;
    private boolean created;
    private String cacheName;
    private SpectratypeChart spectratypeChart;


    public SpectratypeChartCreator(UserFile file, Sample sample) {
        this.file = file;
        this.sp = new Spectratype(false, false);
        this.topclones = sp.addAllFancy(sample, 10);
        this.created = false;
        this.cacheName = CacheType.spectratype.getCacheFileName();
        this.spectratypeChart = new SpectratypeChart();
    }

    public SpectratypeChartCreator create() {
        int[] x_coordinates = sp.getLengths();
        double[] y_coordinates = sp.getHistogram(false);
        int x_min = x_coordinates[0];
        int x_max = x_coordinates[x_coordinates.length - 1];
        spectratypeChart.setxMax(x_max);
        spectratypeChart.setxMin(x_min);
        SpectratypeBar commonSpectratypeBar = new SpectratypeBar("Other", "#666666");
        for (int i = 0; i < x_coordinates.length; i++) {
            commonSpectratypeBar.addPoint((double) x_coordinates[i], y_coordinates[i]);
        }
        spectratypeChart.addBar(commonSpectratypeBar);
        int count = 1;
        for (Clonotype topclone : topclones) {
            SpectratypeBar spectratypeBar = new SpectratypeBar(String.valueOf(count), null, topclone.getCdr3nt(), topclone.getV(), topclone.getJ(), topclone.getCdr3aa());
            int top_clone_x = topclone.getCdr3nt().length();
            for (int i = x_min; i <= x_max; i++) {
                spectratypeBar.addPoint((double) i, i != top_clone_x ? 0 : topclone.getFreq());
            }
            count++;
            spectratypeChart.addBar(spectratypeBar);
        }
        String[] colors = new String[]{"#a50026", "#d73027", "#f46d43", "#fdae61", "#fee090", "#ffffbf", "#74add1", "#abd9e9", "#e0f3f8", "#bababa", "#666666"};
        spectratypeChart.sortByHeight().setColors(colors);
        created = true;
        return this;
    }

    public void saveCache() throws Exception {
        if (created) {
            try {
                File cache = new File(file.getDirectoryPath() + "/" + cacheName + ".cache");
                PrintWriter fileWriter = new PrintWriter(cache.getAbsoluteFile());
                fileWriter.write(Json.stringify(Json.toJson(spectratypeChart)));
                fileWriter.close();
            } catch (FileNotFoundException fnfe) {
                LogAggregator.logServerError("Error while saving cache file[" + file.getFileName() + "," + cacheName + "]", file.getAccount());
                fnfe.printStackTrace();
            }
        } else {
            throw new Exception("You should create graph");
        }
    }

}
