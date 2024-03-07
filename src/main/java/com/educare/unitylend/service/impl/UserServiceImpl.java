package com.educare.unitylend.service.impl;

import com.educare.unitylend.Exception.ServiceException;
import com.educare.unitylend.controller.UserCommunityController;
import com.educare.unitylend.dao.CommunityRepository;
import com.educare.unitylend.dao.UserCommunityRepository;
import com.educare.unitylend.dao.UserRepository;
import com.educare.unitylend.model.User;
import com.educare.unitylend.service.UserCommunityService;
import com.educare.unitylend.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private CommunityRepository communityRepository;
    private UserCommunityController userCommunityController;
    private UserCommunityRepository userCommunityRepository;
    private UserCommunityService userCommunityService;

    @Override
    public List<User> getUsers() throws ServiceException {
        try {
            List<User> userList = userRepository.getAllUsers();
            log.info("userList ", userList);
            return userList;
        } catch (Exception e) {
            log.error("Error encountered during user fetching operation");
            throw new ServiceException("Error encountered during user fetch operation", e);
        }
    }


    @Override
    public User getUserByUserId(String userId) throws ServiceException {
        try {
            User user = userRepository.getUserForUserId(userId);
            return user;
        } catch (Exception e) {
            log.error("Error encountered during user fetch operation");
            throw new ServiceException("Error encountered during user fetch operation", e);
        }
    }


    public void createUser(User newUser) throws ServiceException {
        System.out.println(newUser);
       List<String> matchingCommontags = findMatchingCommontags(newUser);
        System.out.println(newUser);
        System.out.println(matchingCommontags);

        try {
            // Add any validation logic if needed before saving to the database

            for (String tag : matchingCommontags) {
                if (!communityRepository.existsByCommontag(tag)) {

                    communityRepository.createCommunityUsingStrings(tag, tag);
                    }

                }

            userRepository.createUser(newUser);
            newUser.setUserid(userRepository.settingID(newUser.getEmail()));
            newUser.setBorrowingLimit(newUser.getIncome() / 2);
           settingUserRepoMapping(newUser);
        } catch (Exception e) {
            log.error("Error encountered during user creation operation");
            throw new ServiceException("Error encountered during user creation operation", e);
        }
    }
    private void settingUserRepoMapping(User newUser){

        String collegeUni = newUser.getCollegeuniversity();
        String office = newUser.getOfficename();
        String locality = newUser.getLocality();
        if (collegeUni != null) {
            userCommunityRepository.createUserCommunityMapping(newUser.getUserid(), communityRepository.getCommunityIdByName(collegeUni));
        }
        if (office != null) {
            userCommunityRepository.createUserCommunityMapping(newUser.getUserid(), communityRepository.getCommunityIdByName(office));
        }
        if (locality != null) {
            userCommunityRepository.createUserCommunityMapping(newUser.getUserid(), communityRepository.getCommunityIdByName(locality));
        }

    }
    private List<String> findMatchingCommontags(User user) {
        List<String> matchingCommontags = new ArrayList<>();
        String officenameCommontag = null;
        String collegeuniversityCommontag = null;
        String localityCommontag = null;
        List<String> names=new ArrayList<>();
        if (user.getOfficename() != null)  names.add(user.getOfficename());

        if(user.getCollegeuniversity() != null) names.add(user.getCollegeuniversity());

        if(user.getLocality() != null) names.add(user.getLocality());




        Map<String, String> commontags = communityRepository.findCommontagsByNames(names);

            officenameCommontag = commontags.get(user.getOfficename());
            collegeuniversityCommontag = commontags.get(user.getCollegeuniversity());
            localityCommontag = commontags.get(user.getLocality());

        System.out.println(officenameCommontag);
        if (officenameCommontag == null && user.getOfficename() != null) {
            matchingCommontags.add(user.getOfficename());
        }
        if (collegeuniversityCommontag == null && user.getCollegeuniversity() != null) {
            matchingCommontags.add(user.getCollegeuniversity());
        }
        if (localityCommontag == null && user.getLocality() != null) {
            matchingCommontags.add(user.getLocality());
        }


        return matchingCommontags;
    }

    public void updateUser(User user, String userId) throws ServiceException {
        List<String> updatedCommunities;
        String[] communityy=new String[3];
        try {
            userCommunityRepository.deletePrevData(userId);
            userRepository.updateUser(user);
            updatedCommunities = userRepository.findCommunitiesByUserId(userId);
            for(String community:updatedCommunities){
               communityy=community.split(", ");
            }
            for(int i=0;i<3;i++){
                if (!communityRepository.existsByCommontag(communityy[i])) {
                    communityRepository.createCommunityUsingStrings(communityy[i],communityy[i]);
                }
            }
            user.setBorrowingLimit(user.getIncome() / 2);
            settingUserRepoMapping(user);



        } catch (Exception e) {
            log.error("Error encountered during user fetching operation");
            throw new ServiceException("Error encountered during user fetch operation", e);
        }
    }

    public boolean markUserAsInactive(String userId) throws ServiceException {
        try {
            User user = userRepository.getUserForUserId(userId);
            if (user != null) {
                user.setActive(false);
                userRepository.inactivatingUser(user);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new ServiceException("Error marking user as inactive", e);
        }
    }

}
