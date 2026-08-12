package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.SystemAdminRegistrationDTO;
import org.hospitalDonation.v1.entity.SystemAdmin;

import java.util.List;

public interface SystemAdminService {
    List<SystemAdmin>   getAllSystemAdmin();
    void registerSystemAdmin(SystemAdminRegistrationDTO dto);
    SystemAdmin getSystemAdminByUserId(Integer userId);
}