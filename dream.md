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