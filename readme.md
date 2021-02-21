# Dejanko

A library attempting to replicate the major features of Django inside of Ktor with compatibility to an existing Django app for easy and partial migration.

## Features

### Currently implemented:

- Reading from Django's automatically created tables

### Unimplemented but planned:

- Password checking with existing Django user models
- Many-to-many fields
- Structured ORM-style queries
- File fields backed by S3 or Local
- Create and run migrations without Django
- Hook into Django signals
- Admin

### Handled by existing options:

- Templates
    - Handled by [Ktor's templating](https://ktor.io/docs/working-with-views.html) and a myriad of others.
- Serialization
    - Handled by [Jackson](https://github.com/FasterXML/jackson)
- JWT
    - Handled by [Ktor](https://ktor.io/docs/jwt.html#using-a-jwk-provider)
- Logging
    - Handled by [Ktor](https://ktor.io/docs/logging.html#mdc)
- Sessions
    - Handled by [Ktor](https://ktor.io/docs/cookie-header.html)
    - [Example](https://gitlab.com/nanodeath/ktor-session-auth-example/-/blob/master/src/Routes.kt)
    - However, it needs to link into Redis...
    
## Are you dissing on my favorite web server framework, Django?

More accurately Python. We're sick of working with an untyped and extremely dangerous scripting language for large projects.

Django has many brilliant features.  We want to copy them because we *like* them.

## Can I help?

PLEASE.  Both of us on the project work full time and have other projects.

## License

MIT, we'll bother copying it in later