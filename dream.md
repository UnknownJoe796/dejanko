```kotlin

data class SomeModel(
    @PrimaryKey val id: Int = 0,
    val property: String = "Hello!",
    val linked: ForeignKey<Int, OtherModel>? = null
)

data class OtherModel(
    @PrimaryKey val id: Int = 0,
    val property: String = "Hello!"
)

//Query time

SomeModel.query {
    val joined = join(linked)
    where { property equal joined.property }
}

//Ugly time

SomeModel.query {
    val joined = join(this[SomeModel::linked])
    where { this[SomeModel::property] equal joined[OtherModel::property] }
}

//Langhack time - implied owner

SomeModel.query {
    val joined = join(this[::linked])
    where { this[::property] equal joined[::property] }
}

```

## The second DREAM

```kotlin
SomeModel  // SELECT * FROM SomeModel as it
    .filter { it.x > 10 }  // SELECT * FROM SomeModel as it WHERE it.x > 10
    .flatMap { it.parent }  // SELECT * FROM SomeModel as it OUTER JOIN SomeModel as it2 WHERE it2.x > 10
    .distinct()  // SELECT DISTINCT * FROM SomeModel as it OUTER JOIN SomeModel as it2 WHERE it2.x > 10
    .sortedBy { it.z }  // SELECT * FROM SomeModel as it OUTER JOIN SomeModel as it2 WHERE it2.x > 10 ORDER BY it.z
    .take(100)  // SELECT * FROM SomeModel as it OUTER JOIN SomeModel as it2 WHERE it2.x > 10 ORDER BY it.z LIMIT 100
    .skip(100)  // SELECT * FROM SomeModel as it OUTER JOIN SomeModel as it2 WHERE it2.x > 10 ORDER BY it.z LIMIT 100 OFFSET 100

SomeModel  // SELECT * FROM SomeModel as it
    .flatMap { a -> OtherModel.filter { b -> a.x equals b.x } }  // SELECT * FROM SomeModel as a RIGHT JOIN OtherModel as b ON a.x = b.x

SomeModel
    .maxBy { it.x }

SomeModel
    .map { it.manyToMany.maxBy { it.x } }

SomeModel
    .filter { it.manyToMany.map { it.x }.max() lessThan 12 }
// SELECT DISTINCT *,  FROM SomeModel LEFT JOIN OtherModel ON ... WHERE 
// SELECT * FROM 

//SELECT a.lecturerName, b.NumOfModules
//FROM Lecturer a,(
//        SELECT l.lecturerID, COUNT(moduleID) AS NumOfModules
//FROM Lecturer l , Teaches t
//        WHERE l.lecturerID = t.lecturerID
//        AND year = 2011
//GROUP BY l.lecturerID) b
//WHERE a.lecturerID = b.lecturerID

Lecturer
    .map { a -> Teaches.filter { b -> a.lecturerID equals b.lecturerID }.count() }
```

What does a query set have?

- Data set it is based on (directly a table, or perhaps another query)
- Filter conditions
- (if sorted) Sort
- (if sorted) Limit
- (if sorted) Offset

Subquery leads to joining subquery

```kotlin
SomeModel
    .filter { it.manyToMany.map { it.x }.max() lessThan 12 }
```

```sql
SELECT subquery.val FROM SomeModel
    LEFT JOIN (
        SELECT main.id AS pk, MAX(other.x) AS val
        FROM SomeModel as main
        JOIN ThroughModel as through ON main.id = through.SomeModelId
        JOIN OtherModel as other ON through.OtherModelId = other.id
        GROUP BY main.idk
    ) as subquery ON subquery.pk = pk
    WHERE someCondition
```