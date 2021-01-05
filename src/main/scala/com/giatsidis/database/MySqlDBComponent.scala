package com.giatsidis.database

import com.giatsidis.spark.Config

trait MySqlDBComponent extends DBComponent {

  val driver = slick.jdbc.MySQLProfile

  import driver.api._

  val url = s"jdbc:mysql://${Config.dbHost}:${Config.dbPort}/${Config.dbName}?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&useSSL=false";

  val db: Database = Database.forURL(
    url = url,
    user = Config.dbUsername,
    password = Config.dbPassword,
    driver = "com.mysql.jdbc.Driver"
  )

}