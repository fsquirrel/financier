package com.fsquirrelsoft.financier.ui.report;

import android.content.Context;
import android.content.Intent;

import com.fsquirrelsoft.financier.data.AccountType;
import com.fsquirrelsoft.financier.data.Balance;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;

import java.text.DecimalFormat;
import java.util.List;

public class BalancePieChart extends AbstractChart {

    DecimalFormat percentageFormat = new DecimalFormat("##0");

    public BalancePieChart(Context context, int orientation, float dpRatio) {
        super(context, orientation, dpRatio);
    }

    public Intent createIntent(AccountType at, List<Balance> balances) {
        double total = 0D;
        for (Balance b : balances) {
            total += b.getMoney() <= 0 ? 0 : b.getMoney();
        }
        CategorySeries series = new CategorySeries(at.getDisplay(resources));
        for (Balance b : balances) {
            if (b.getMoney() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(b.getName());
                double p = (b.getMoney() * 100) / total;
                if (p >= 1) {
                    sb.append("(").append(percentageFormat.format(p)).append("%)");
                    series.add(sb.toString(), b.getMoney());
                }
            }
        }
        int[] color = createColor(series.getItemCount());
        DefaultRenderer renderer = buildCategoryRenderer(color);
        renderer.setLabelsTextSize(14 * dpRatio);
        renderer.setLegendTextSize(16 * dpRatio);
        return ChartFactory.getPieChartIntent(context, series, renderer, series.getTitle());
    }
}
