package com.ivieleague.dejanko

import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.LocalDate

@DjangoPath("coupon", "coupon")
data class Coupon(
    @PrimaryKey val id: Int,
    @MaxLength(100) val name: String,
    @MaxLength(500) val description: String,
    val creationTime: DateTime,
    @Index @OnDelete(DeleteBehavior.Cascade) val business: ForeignKey<Int, Business>,
    val image: MediaFile,
    val enabled: Boolean = true,
    val expirationDate: LocalDate
)

@DjangoPath("business", "business")
data class Business(
    @PrimaryKey val id: Int
)

fun main(vararg args: String) {
    val db = PostgreSQLConnectionBuilder.createConnectionPool {
        host = "localhost"
        port = 5432
        username = "postgres"
        password = "postgres"
    }.asSuspending

    println(Coupon::class.dbInfo)

    println("Launching...")
    runBlocking {
        db.connect()
        val rows = db.sendQuery(Coupon::class.dbInfo.queryStart())
        val instances = rows.rows.parsed<Coupon>()
        for(instance in instances){
            println(instance)
        }
        db.disconnect()
    }
}