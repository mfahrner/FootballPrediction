package com.theironyard.charlotte.services;

import com.theironyard.charlotte.entities.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by mfahrner on 9/18/16.
 */
public interface TeamRepository extends PagingAndSortingRepository<Team, Integer> {
    Team findByNameIgnoreCase(String name);
}
