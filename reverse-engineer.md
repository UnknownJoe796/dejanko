# Reverse Engineering Django ORM

```kotlin
val tableName = "${appName}_${modelName.toLowercase()}"
val manyManyTableName = "${appName}_${modelName.toLowercase()}_${fieldName}"
```

Field name is direct

`id` is autoincrementing number, default `nextval('business_business_id_seq'::regclass)`

Many-many fields: auto-incrementing `id`, FKs `"$modelName_id"`

Example: `"business_business_ad_business_id_ba9b75c8_fk_business_" FOREIGN KEY (business_id) REFERENCES business_business(id) DEFERRABLE INITIALLY DEFERRED`

DateTimeField -> timestamp with time zone

DateField -> date


## Content Type

Table name: `django_content_type`

Fields: 
- `id`, autoincrementing
- `app_label`
- `model`

## Migrations

```
 Column  |           Type           | Collation | Nullable |                    Default
---------+--------------------------+-----------+----------+-----------------------------------------------
 id      | integer                  |           | not null | nextval('django_migrations_id_seq'::regclass)
 app     | character varying(255)   |           | not null |
 name    | character varying(255)   |           | not null |
 applied | timestamp with time zone |           | not null |

```