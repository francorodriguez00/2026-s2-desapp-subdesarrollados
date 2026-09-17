# Data Model

## Entities

### User
- `id` (Long, PK)
- `email` (String, unique, index)
- `name` (String)
- `password` (String, hashed)
- `portfolio` (OneToOne relationship)

### Portfolio
- `id` (Long, PK)
- `user` (OneToOne, inverse)
- `tokens` (List of Tokens, initial empty)

## Validation Rules
- `email`: NotNull, Pattern(@), Unique.
- `password`: NotNull, minLength 8.
- `name`: NotNull.

## State Transitions
- User Registered -> Portfolio Created
