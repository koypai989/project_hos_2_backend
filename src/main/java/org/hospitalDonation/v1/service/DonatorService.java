package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.DonatorRegistrationDTO;
import org.hospitalDonation.v1.entity.Donator;

import java.util.List;

public interface DonatorService {
    Donator getDonatorByCitizenId(String citizenId);
    List<Donator> getAllDonators();
    void registerDonator(DonatorRegistrationDTO dto);
    Donator getDonatorByUserId(Integer userId);
    void updateDonator(Integer userId, Donator updateData);
}