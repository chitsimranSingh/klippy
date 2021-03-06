package com.chitsimran.klippy.service;

import com.chitsimran.klippy.exceptions.ClipDataNotFound;
import com.chitsimran.klippy.exceptions.UserNotFoundException;
import com.chitsimran.klippy.mongo.model.ClipDataEntity;
import com.chitsimran.klippy.mongo.model.UserEntity;
import com.chitsimran.klippy.mongo.repository.ClipDataRepository;
import com.chitsimran.klippy.mongo.repository.UserRepository;
import com.chitsimran.klippy.pojo.ClipData;
import com.chitsimran.klippy.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClipDataService {

    private final ClipDataRepository clipDataRepository;
    private final UserRepository userRepository;

    @Autowired
    public ClipDataService(ClipDataRepository clipDataRepository, UserRepository userRepository) {
        this.clipDataRepository = clipDataRepository;
        this.userRepository = userRepository;
    }


    public List<ClipData> getUserClips(String userName) {
        Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        if (!userEntity.isPresent()) {
            throw new UserNotFoundException(userName);
        }
        List<ClipDataEntity> clipDataEntities = clipDataRepository.findByUserEntity(userEntity.get());
        log.info("USER_CLIPS_FETCHED: for user: {}, clips: {}", userName, JsonUtil.toJson(clipDataEntities));
        return buildClipDataListFromClipDataEntityList(clipDataEntities);
    }

    public void addClipData(String userName, ClipData clipData) {
        Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        if (!userEntity.isPresent())
            throw new UserNotFoundException(userName);
        ClipDataEntity clipDataEntity = ClipDataEntity.builder()
                .clipData(clipData.getData())
                .userEntity(userEntity.get())
                .build();
        clipDataRepository.save(clipDataEntity);
        log.info("CLIP_DATA_SAVED: for user: {}, clip data: {}", userName, JsonUtil.toJson(clipDataEntity));
    }

    private List<ClipData> buildClipDataListFromClipDataEntityList(List<ClipDataEntity> clipDataEntities) {
        List<ClipData> clipDataList = new ArrayList<>();
        for (ClipDataEntity clipDataEntity : clipDataEntities) {
            ClipData clipData = ClipData.builder()
                    .id(clipDataEntity.getId().toString())
                    .userName(clipDataEntity.getUserEntity().getUserName())
                    .data(clipDataEntity.getClipData())
                    .build();
            clipDataList.add(clipData);
        }
        return clipDataList;
    }

    public void deleteClipData(String userName, String id) {
        Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        if (!userEntity.isPresent())
            throw new UserNotFoundException(userName);
        log.info("DELETING_USER_CLIP: for user: {}, clip id: {}", userName, id);
        clipDataRepository.deleteById(new ObjectId(id));
    }

    public void updateClipData(String userName, ClipData clipData) {
        Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        if (!userEntity.isPresent())
            throw new UserNotFoundException(userName);
        Optional<ClipDataEntity> clipDataEntity = clipDataRepository.findById(new ObjectId(clipData.getId()));
        if (!clipDataEntity.isPresent())
            throw new ClipDataNotFound(userName, clipData.getId());
        clipDataEntity.get().setClipData(clipData.getData());
        clipDataRepository.save(clipDataEntity.get());
        log.info("UPDATE_CLIP: id: {}, data: {}", clipData.getId(), JsonUtil.toJson(clipData.getData()));
    }
}
