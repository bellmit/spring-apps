package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.service.EsService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Component
public class WeiBao {
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private TransportClient client;
    @Autowired
    private EsService esService;

    private final static String HBASENAME = "DATAETL:RPT_IND";

    public String getchkday(String rptid) {

        SearchRequestBuilder srb = client.prepareSearch("reportlabel").setSize(1)
                .setQuery(matchQuery("healthReportId", rptid.trim()));
        SearchHit[] searchHits = srb.execute().actionGet().getHits().getHits();
        return stream(searchHits).map(x -> x.getSourceAsMap().get("lastUpdateTime")).findFirst().orElse("").toString();
    }

    public List getRep1(String rptid) {

        List list = new ArrayList();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        // SubstringComparator substringComparator = new SubstringComparator(rptid);
        String day = getchkday(rptid);
        StringBuffer sb = new StringBuffer(day);
        String day1 = day.substring(0, 10);
        StringBuffer sb1 = new StringBuffer(day1);
        StringBuffer rowkey = sb1.append("_" + rptid + "_");
        String endrowkey = day1 + "_" + (Integer.parseInt(rptid) + 1) + "_";


        Scan scan = new Scan();
        // scan.setFilter(filter);
        scan.setStartRow(rowkey.toString().getBytes());
        scan.setStopRow(endrowkey.getBytes());
        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
                System.out.println(key+","+value);
            }
            return list1;
        });
        return list1;
    }
}
