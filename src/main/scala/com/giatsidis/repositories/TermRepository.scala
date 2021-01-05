package com.giatsidis.repositories

import com.giatsidis.database.models.Term
import com.giatsidis.database.{DBComponent, MySqlDBComponent}
import slick.lifted.ProvenShape

import scala.concurrent.Future

class TermRepository extends TermTable {
  this: DBComponent =>

  import driver.api._

  def getAll(): Future[List[Term]] = db.run {
    termTableQuery.to[List].result
  }
}

private[repositories] trait TermTable {
  this: DBComponent =>

  import driver.api._

  private[TermTable] class TermTable(tag: Tag) extends Table[Term](tag, "Terms") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")
    val keywords = column[String]("keywords")

    def * : ProvenShape[Term] = (id.?, name, keywords) <> (Term.tupled, Term.unapply)

  }

  protected val termTableQuery = TableQuery[TermTable]
}

object TermRepository extends TermRepository with MySqlDBComponent
