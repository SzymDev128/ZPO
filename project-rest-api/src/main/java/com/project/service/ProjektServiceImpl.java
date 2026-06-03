package com.project.service;

import com.project.model.Projekt;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjektServiceImpl implements ProjektService {

    private ProjektRepository projektRepository;
    private ZadanieRepository zadanieRepository;

    @Autowired
    public ProjektServiceImpl(ProjektRepository projektRepository, ZadanieRepository zadanieRepository){
        this.projektRepository = projektRepository;
        this.zadanieRepository = zadanieRepository;
    }

    @Override
    public Optional<Projekt> getProjekt(Integer projektId) {
        return projektRepository.findById(projektId);
    }

    @Override
    public Projekt setProjekt(Projekt projekt) {
        return projektRepository.save(projekt);
    }

    @Override
    @Transactional
    public void deleteProjekt(Integer projektId) {
        zadanieRepository.deleteAll(zadanieRepository.findZadaniaProjektu(projektId));
        projektRepository.deleteById(projektId);
    }

    @Override
    public Page<Projekt> getProjekty(Pageable pageable) {
        return projektRepository.findAll(pageable);
    }

    @Override
    public Page<Projekt> searchByNazwa(String nazwa, Pageable pageable) {
        return projektRepository.findByNazwaContainingIgnoreCase(nazwa,pageable);
    }
}
