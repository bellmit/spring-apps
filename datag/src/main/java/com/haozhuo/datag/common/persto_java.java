package com.haozhuo.datag.common;

import com.facebook.presto.jdbc.PrestoConnection;
import com.facebook.presto.jdbc.PrestoStatement;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimeZone;

public class persto_java {


//    连接mysql数据库并且插入presto数据到mysql rpt_ind表
    public static void connetMysql(ArrayList<ArrayList> prestoRptindResult ) {


    }



        public static void printRow(ResultSet rs,int[]types) throws SQLException

        {

            for(int i=0;i<types.length;i++)

                System.out.print(rs.getObject(i+1));

            System.out.println("");

        }

//        连接presto拉取rpt_ind表所有数据
        public static ArrayList<String> connect(String sql,String column) throws SQLException {


            //设置时区，这里必须要设置

            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

            try {

                Class.forName("com.facebook.presto.jdbc.PrestoDriver");

            } catch (ClassNotFoundException e) {

                e.printStackTrace();

            }

            PrestoConnection connection = null;

            try {

                //连接字符串中的hive是catalog名字，dataetl是schema名字，root是用户名，这个用户名根据实际业务自己设定，用来标示执行sql的用户，但是不会通过该用户名进行身份认证，但是必须要写。密码直接指定为null

                connection = (PrestoConnection) DriverManager.getConnection(

                        "jdbc:presto://212.64.43.16:7080", "root", null);

            } catch (SQLException e) {

                e.printStackTrace();

            }

            PrestoStatement statement = null;

            try {


                statement = (PrestoStatement) connection.createStatement();

            } catch (SQLException e) {

                e.printStackTrace();

            }


            //初始化查询
            String query = "";
            ArrayList<ArrayList> prestoRptindResult = new ArrayList<>();





            ResultSet rs = null;


            try {

                rs = statement.executeQuery(sql);

            } catch (SQLException e) {

                e.printStackTrace();

            }

            String[] col = column.split("_");

            ArrayList<String> dataList = new ArrayList<>();
            while (rs.next()){

                StringBuilder row = new StringBuilder();
                for (int i = 0; i <col.length ; i++) {
                row.append(rs.getString(col[i])+"_");

                }
                String row_result = row.substring(0, row.length() - 1);

                dataList.add(row_result);


            }
            //返回一个数组
            return dataList;

        }

        public static void main(String[] args) throws SQLException {


            connect("select a.order_no,a.name, b.goods_name,case a.sex when 0 then '女' when 1 then '男' else '其他' end as sex,a.age, a.order_main_no,case a.marry when 1 then '已婚' when 2 then '未婚' else '其他' end as marry,c.city_name,a.check_unit_code,f.label from mysql.bisys.physical_user_check_info as a left join mysql.bisys.goods_info as b on a.goods_id = b.goods_id left join mysql.bisys.city_factory as c on a.check_unit_code = c.factory_code LEFT JOIN mysql_mall.yjk_mall.mall_order as d on a.order_no = cast(d.order_no as varchar) LEFT JOIN hive.dataetl.rpt_user as e on d.user_id = e.uid LEFT JOIN hive.dataetl.rpt_labels as f on e.rpt_id = f.rpt_id LEFT JOIN hive.dataetl.rpt_b as g on f.rpt_id = g.rpt_id and g.chk_date > '2018-01-01' and g.chk_date < '2019-01-01' limit 2000","aa_bb");

        }

    }





