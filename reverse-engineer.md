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

```
                                     Table "public.django_content_type"
  Column   |          Type          | Collation | Nullable |                     Default
-----------+------------------------+-----------+----------+-------------------------------------------------
 id        | integer                |           | not null | nextval('django_content_type_id_seq'::regclass)
 app_label | character varying(100) |           | not null |
 model     | character varying(100) |           | not null |
Indexes:
    "django_content_type_pkey" PRIMARY KEY, btree (id)
    "django_content_type_app_label_model_76bd3d3b_uniq" UNIQUE CONSTRAINT, btree (app_label, model)
Referenced by:
    TABLE "auth_permission" CONSTRAINT "auth_permission_content_type_id_2f476e4b_fk_django_co" FOREIGN KEY (content_type_id) REFERENCES django_content_type(id) DEFERRABLE INITIALLY DEFERRED
    TABLE "django_admin_log" CONSTRAINT "django_admin_log_content_type_id_c4bce8eb_fk_django_co" FOREIGN KEY (content_type_id) REFERENCES django_content_type(id) DEFERRABLE INITIALLY DEFERRED
```

## Migrations

```
                      Table "public.django_migrations"
 Column  |           Type           | Collation | Nullable |                    Default
---------+--------------------------+-----------+----------+-----------------------------------------------
 id      | integer                  |           | not null | nextval('django_migrations_id_seq'::regclass)
 app     | character varying(255)   |           | not null |
 name    | character varying(255)   |           | not null |
 applied | timestamp with time zone |           | not null |

```

## Sessions

```
                      Table "public.django_session"
    Column    |           Type           | Collation | Nullable | Default
--------------+--------------------------+-----------+----------+---------
 session_key  | character varying(40)    |           | not null |
 session_data | text                     |           | not null |
 expire_date  | timestamp with time zone |           | not null |
Indexes:
    "django_session_pkey" PRIMARY KEY, btree (session_key)
    "django_session_expire_date_a5c62663" btree (expire_date)
    "django_session_session_key_c0390e0f_like" btree (session_key varchar_pattern_ops)

```

Session data is base64 JSON:

Encoded: 

```
MDVmOGZhNDc5MjliNGQyNzE3YmQ3MjdhMTBlZmE0NTc5M2Q3NGIxMTp7Il9hdXRoX3VzZXJfaWQiOiIxIiwiX2F1dGhfdXNlcl9iYWNrZW5kIjoiZGphbmdvLmNvbnRyaWIuYXV0aC5iYWNrZW5kcy5Nb2RlbEJhY2tlbmQiLCJfYXV0aF91c2VyX2hhc2giOiI4YjhkNTFiMGNjNmRiNWFjYjNiZjdjYTM1N
mVkNDRmMDk5MjkxNDNjIn0=
```

Decoded:

```
05f8fa47929b4d2717bd727a10efa45793d74b11:{"_auth_user_id":"1","_auth_user_backend":"django.contrib.auth.backends.ModelBackend","_auth_user_hash":"8b8d51b0cc6db5acb3bf7ca356ed44f09929143c"}
```

[Theoretical explanation, doesn't seem to line up](https://github.com/django/django/blob/master/django/core/signing.py)

Actual explanation, after in-depth search and manual checking:

```kotlin
val salt = "django.contrib.sessionsSessionStore"
val secret = "whatever server secret"
val hmacKey = sha1(salt + secret)
val signature = hmac(key = hmacKey, value = dataToSign)
val result = signature.hex() + ":" + dataToSign
val base64ed = result.base64()
```

## Users

Implemented on top of Django's ORM, so you can use the models directly instead!

[django/contrib/auth/models.py](https://github.com/django/django/blob/master/django/contrib/auth/models.py)


## Django Q, ORM backend

Models can be found [here](https://github.com/Koed00/django-q/blob/master/django_q/models.py)

This library relies on pickling, which is a Python-specific serialization method.

Direct integration is likely not possible.

Perhaps indirect integration is a better option anyways - a message-passing implementation between Kotlin and Python is needed anyways.

## Celery

Celery uses JSON by default, so we can integrate here, but only if they don't use DJCelery, which uses pickling.

## File Fields

S3 example: `us-west-2:2fecf7c8-eb80-4e40-82d4-08aefbb3d147/0b4f1e72-659e-4880-8a37-402fb7f270a7.mp4`

By the looks of it, interpretation of the path is up to the implementation.