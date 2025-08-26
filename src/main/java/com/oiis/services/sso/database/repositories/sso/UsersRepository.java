package com.oiis.services.sso.database.repositories.sso;

import com.oiis.services.sso.database.entities.sso.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends BaseSsoRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
