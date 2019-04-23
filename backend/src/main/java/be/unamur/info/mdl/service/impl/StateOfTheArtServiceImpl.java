package be.unamur.info.mdl.service.impl;

import be.unamur.info.mdl.dal.entity.StateOfTheArtEntity;
import be.unamur.info.mdl.dal.repository.StateOfTheArtRepository;
import be.unamur.info.mdl.dal.repository.UserRepository;
import be.unamur.info.mdl.dto.StateOfTheArtDTO;
import be.unamur.info.mdl.service.StateOfTheArtService;
import be.unamur.info.mdl.service.exceptions.NoSotaException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("stateOfTheArtService")
@Transactional
public class StateOfTheArtServiceImpl implements StateOfTheArtService {
  private StateOfTheArtRepository stateOfTheArtRepository;
  private  UserRepository userRepository;

@Autowired
  public StateOfTheArtServiceImpl( StateOfTheArtRepository sotaRepository, UserRepository userRepository){

    this.stateOfTheArtRepository =sotaRepository ;
    this.userRepository = userRepository;
  }

  @Override
  public StateOfTheArtDTO getStateOfTheArt (String reference) throws NoSotaException{
    if(reference == null){
      throw new IllegalArgumentException("The reference must be defined");
    }
    Optional<StateOfTheArtEntity> dbSota = this.stateOfTheArtRepository.findDistinctByNameLike(reference);
    if(!dbSota.isPresent()){
      throw new NoSotaException("The referenced article was not found");
    } else {
      return dbSota.get().toDTO();
    }

  }




}

