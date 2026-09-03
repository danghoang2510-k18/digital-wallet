package com.example.digital_wallet.user.repository;


import com.example.digital_wallet.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PermissionRepository extends JpaRepository<Permission,String> {
}
