package com.oiis.services.sso.database.repositories.sso;

import com.oiis.services.sso.database.entities.sso.BaseSsoTableModel;
import com.oiis.services.sso.database.repositories.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseSsoRepository<M extends BaseSsoTableModel, ID> extends BaseRepository<M, ID> {

}