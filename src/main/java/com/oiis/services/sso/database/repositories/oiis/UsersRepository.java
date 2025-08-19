package com.oiis.services.sso.database.repositories.oiis;

import com.oiis.services.sso.database.entities.oiis.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends BaseOiisRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
