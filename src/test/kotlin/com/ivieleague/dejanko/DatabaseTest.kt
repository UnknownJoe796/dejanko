package com.ivieleague.dejanko

import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import com.ivieleague.dejanko.orm.*
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class DatabaseTest {

    @DjangoPath("demo", "ModelA")
    data class ModelA(
        @PrimaryKey val id: Int,
        val integerField: Int,
        val decimalField: BigDecimal,
        val durationField: Period,
        val fileField: String,
        val floatField: Double,
        val booleanField: Boolean,
        val jsonField: String,
        val uuidField: UUID,
        @MaxLength(200) val charField: String,
        val dateField: LocalDate,
        val timeField: LocalTime,
        val dateTimeField: DateTime,
        @Index @OnDelete(DeleteBehavior.SetNull) val foreignKeyRecursiveField: ForeignKey<Int, ModelA>? = null,
        @Index @OnDelete(DeleteBehavior.SetNull) val foreignKeyField: ForeignKey<Int, ModelB>? = null,
    )

    @DjangoPath("demo", "ModelB")
    data class ModelB(
        @PrimaryKey val id: Int,
        val name: String
    )

    @Test
    fun test(){
        val db = PostgreSQLConnectionBuilder.createConnectionPool {
            host = "localhost"
            port = 5432
            username = "postgres_user"
            password = "postgres_pass"
            database = "default"
        }.asSuspending
        Settings.defaultDb = db

        println(ModelA::class.dbInfo)
        println(ModelB::class.dbInfo)

        println("Launching...")
        runBlocking {
            db.connect()
            val rows = db.sendQuery(ModelA::class.dbInfo.queryStart())
            val instances = rows.rows.parsed<ModelA>()
            for(instance in instances){
                println(instance)
                println("  Linked to:")
                println("  " + instance.foreignKeyRecursiveField?.resolve())
                println("  " + instance.foreignKeyField?.resolve())
            }
            db.disconnect()
        }
    }
}