package com.example.library_backend.users

fun User.toGetDto() = UserGetDto(
    id = id,
    username = username,
    email = email,
    fullName = fullName,
    phone = phone,
    role = role,

)

fun User.updateFrom(dto: UserUpdateDto) {
    //Complicated to do right now
//    username = dto.username
//    email = dto.email
    fullName = dto.fullName
    phone = dto.phone
    if (dto.role != null) {
        role = dto.role
    }
}