package com.oiis.services.sso.database.repositories;

import com.oiis.services.sso.database.entities.BaseTableModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<M extends BaseTableModel, ID> extends JpaRepository<M, ID>, JpaSpecificationExecutor<M> {

}