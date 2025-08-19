package com.oiis.services.sso.database.repositories.oiis;

import com.oiis.services.sso.database.entities.oiis.BaseOiisTableModel;
import com.oiis.services.sso.database.repositories.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseOiisRepository<M extends BaseOiisTableModel, ID> extends BaseRepository<M, ID> {

}