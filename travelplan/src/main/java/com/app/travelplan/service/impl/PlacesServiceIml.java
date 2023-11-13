package com.app.travelplan.service.impl;

import com.app.travelplan.model.dto.PlacesDto;
import com.app.travelplan.model.entity.*;
import com.app.travelplan.model.form.PlacesForm;
import com.app.travelplan.repository.*;
import com.app.travelplan.service.GeneralService;
import com.app.travelplan.service.PlacesService;
import com.app.travelplan.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacesServiceIml implements PlacesService {
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PlacesRepository placesRepository;
    private final AddressRepository addressRepository;
    private final LinkRepository linkRepository;
    private final FollowRepository followRepository;

    private final GeneralService generalService;

    @Override
    public PlacesDto save(PlacesForm placesForm) {
        Places places = toEntity(placesForm);

        places.getUser().getPlaces().add(places);
        return PlacesDto.toDto(placesRepository.save(places));
    }

    @Override
    public List<PlacesDto> getAll() {
        return placesRepository.findAll()
                .stream()
                .map(PlacesDto::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(long id) {
        Places places = placesRepository.findById(id)
                .orElseThrow(()->
                        new NotFoundException("Places not found with id " + id));

        if(places.getUser().getUsername().equals(SecurityUtils.getUsernameOfPrincipal())) {
            places.getUser().getPlaces().remove(places);
            placesRepository.delete(places);
            return;
        }
        throw new IllegalArgumentException("Delete places not permit");
    }

    @Override
    public PlacesDto update(PlacesForm placesForm, long id) {
        Places places = placesRepository.findById(id)
                .orElseThrow(()->
                        new NotFoundException("Places not found with id " + id));

        if(places.getUser().getUsername().equals(SecurityUtils.getUsernameOfPrincipal())) {
            Places p = toEntity(placesForm);
            p.setId(id);
            return PlacesDto.toDto(placesRepository.save(p));
        }
        throw new IllegalArgumentException("Update places not permit");
    }

    @Override
    public PlacesDto getById(long id) {
        return PlacesDto.toDto(placesRepository.findById(id)
                .orElseThrow(()->
                        new NotFoundException("Places not found with id " + id)));
    }

    @Override
    public List<PlacesDto> getMyFollow() {
        Follow follow = followRepository.findByUser_Username(SecurityUtils.getUsernameOfPrincipal())
                .orElseThrow(()->
                        new NotFoundException("Follow not found with username " + SecurityUtils.getUsernameOfPrincipal()));
        return follow.getPlaces()
                .stream()
                .map(PlacesDto::toDto)
                .collect(Collectors.toList());
    }

    private Places toEntity(PlacesForm placesForm) {
        if(placesForm.getBeginDay().isAfter(placesForm.getEndDay())) {
            throw new IllegalArgumentException("Begin day must be before end day");
        }
        List<Image> images = generalService.getAllImageByArrayId(placesForm.getImageId());
        List<Category> categories = generalService.getAllCategoryByArrayId(placesForm.getCategoryId());
        //du lịch lớn hơn 4tr/ng:
        String s = "";
        if(placesForm.getCost().compareTo(BigDecimal.valueOf(500000)) >= 0) {
            s="Cao cấp";
        } else if(placesForm.getCost().compareTo(BigDecimal.valueOf(150000)) >= 0) {
            s="Cao";
        } else if(placesForm.getCost().compareTo(BigDecimal.valueOf(50000)) >= 0) {
            s="Vừa";
        } else {
            s="Bình dân";
        }
        categories.add(categoryRepository.findByName(s).get());

        User user = userRepository.findByUsername(SecurityUtils.getUsernameOfPrincipal())
                .orElseThrow(()->
                        new UsernameNotFoundException("Username not found with " + SecurityUtils.getUsernameOfPrincipal()));

        Address address = addressRepository.findById(placesForm.getAddressId())
                .orElseThrow(()->
                        new NotFoundException("Category not fount with id "+placesForm.getAddressId()));

        Link link = linkRepository.findById(placesForm.getLinkId())
                .orElseThrow(()->
                        new NotFoundException("Category not fount with id "+placesForm.getLinkId()));

        Places places = Places.builder()
                .title(placesForm.getTitle())
                .phoneNumber(placesForm.getPhoneNumber())
                .cost(placesForm.getCost())
                .point(0)
                .description(placesForm.getDescription())
                .timePlaces(placesForm.getTimePlaces())
                .user(user)
                .categories(categories)
                .images(images)
                .link(link)
                .timePlaces(placesForm.getTimePlaces())
                .address(address)
                .isFull(placesForm.isFull())
                .beginDay(placesForm.getBeginDay())
                .endDay(placesForm.getEndDay())
                .build();
        places.getAddress().setPlaces(places);
        places.getLink().setPlaces(places);

        return places;
    }

}
