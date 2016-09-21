package com.theironyard.charlotte.controllers;

import com.theironyard.charlotte.entities.Team;
import com.theironyard.charlotte.services.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Created by mfahrner on 9/18/16.
 */

@Controller
public class FootballController {

    @Autowired
    TeamRepository teams;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, String offPass, String offRush, String defPass, String defRush) {
        List<Team> teamList;

        if (null != offPass) {
            teamList = (List)teams.findAll(new Sort (DESC, "offPass"));
        } else if (null != offRush) {
            teamList = (List)teams.findAll(new Sort (DESC, "offRush"));
        } else if (null != defPass) {
            teamList = (List)teams.findAll(new Sort (ASC, "defPass"));
        } else if (null != defRush) {
            teamList = (List)teams.findAll(new Sort (ASC, "defRush"));
        } else {
            teamList = (List)teams.findAll();
        }
        model.addAttribute("teams", teamList);
        return "home";
    }


    @RequestMapping(path = "/", method = RequestMethod.POST)
    public String generateProb(Model model, String homeName, String awayName) {

        List<Team> teamList;
        teamList = (List)teams.findAll();

        Team homeTeam = teams.findByNameIgnoreCase(homeName);
        Team awayTeam = teams.findByNameIgnoreCase(awayName);
        Team averageTeam = teams.findOne(33);

        double homeOppOdds = homeTeam.computeOddsFromProb(homeTeam.getAdjustedAverageOppGWP());
        double homeOppLogit = homeTeam.computeLogitFromOdds(homeOppOdds);

        double awayOppOdds = awayTeam.computeOddsFromProb(awayTeam.getAdjustedAverageOppGWP());
        double awayOppLogit = awayTeam.computeLogitFromOdds(awayOppOdds);
        
        double ultimateHomeLogit = homeTeam.computeFinalLogit(homeTeam.getTeamLogit(), homeOppLogit,
                awayTeam.getTeamLogit(), awayOppLogit);

        double homeTeamOdds = homeTeam.computeOdds(ultimateHomeLogit);

        DecimalFormat numberFormat = new DecimalFormat("#.00");

        double homeTeamProbWin = homeTeam.computeProbability(homeTeamOdds);
        double homeTeamPer = Double.valueOf(numberFormat.format(homeTeamProbWin * 100));

        double awayTeamProbWin = 1 - homeTeamProbWin;
        double awayTeamPer = Double.valueOf(numberFormat.format(awayTeamProbWin * 100));

        model.addAttribute("homeTeamWin", homeTeamPer);
        model.addAttribute("awayTeamWin", awayTeamPer);
        model.addAttribute("teams", teamList);
        return "homePlus";
    }

    @PostConstruct
    public void init() throws IOException {
        if (teams.count() == 0) {
            File f = new File("stats.csv");
            Scanner fileScanner = new Scanner(f);

            while (fileScanner.hasNext()) {

                String line = fileScanner.nextLine();
                String[] columns = line.split(",");

                Team team = new Team();

                ArrayList<Integer> oppList = new ArrayList<>();
                oppList.add(Integer.valueOf(columns[8]));
                oppList.add(Integer.valueOf(columns[9]));

                double teamLogit = team.computeTeamLogit(Double.valueOf(columns[1]), Double.valueOf(columns[2]),
                        Double.valueOf(columns[5]), Double.valueOf(columns[6]), Double.valueOf(columns[3]),
                        Double.valueOf(columns[4]), Double.valueOf(columns[7]));

                team = new Team(columns[0], Double.valueOf(columns[1]), Double.valueOf(columns[2]),
                        Double.valueOf(columns[5]), Double.valueOf(columns[6]), Double.valueOf(columns[3]),
                        Double.valueOf(columns[4]), Double.valueOf(columns[7]), teamLogit, oppList, null, null,
                        null, null, null);

                teams.save(team);
            }
        }

        for (int i = 0; i < teams.count();i++) {
            Team current = teams.findOne(i + 1);
            Team averageTeam = teams.findOne(33);
            double GWPLogit = current.computeGWPLogit(current.getTeamLogit(), averageTeam.getTeamLogit());
            double GWPodds = current.computeOdds(GWPLogit);
            double GWP = current.computeProbability(GWPodds);
            current.setGWP(GWP);
            teams.save(current);
            // correct up to here GWP
            // need to figure oppAverageGWP
        }
        // correct up to here next up is adjustment for SOS to figure adjustedGWP
        for (int i = 0; i < teams.count();i++) {
            Team current = teams.findOne(i + 1);
            double oppAverageGWP = computeOppGWP(current.getOpponents());
            current.setAverageOppGWP(oppAverageGWP);
            teams.save(current);
        }

        // correct up to here iteration is next
        for (int i = 0; i < teams.count();i++) {
            Team current = teams.findOne(i + 1);
            Team average = teams.findOne(33);
            double oppGWPOdds = current.computeOddsFromProb(current.getAverageOppGWP());
            double oppGWPLogit = current.computeLogitFromOdds(oppGWPOdds);
            double adjustedTeamLogit = current.computeAdjustedGWPLogit(current.getTeamLogit(), average.getTeamLogit(),
                    oppGWPLogit);
            double adjustedOdds = current.computeOdds(adjustedTeamLogit);
            double adjustedGwp = current.computeProbability(adjustedOdds);
            current.setAdjustedGWP(adjustedGwp);
            teams.save(current);
        }

        for (int i = 0; i < 5; i++) {
            // correct up to here adjusted opp GWP
            for (int j = 0; j < teams.count(); j++) {
                Team current = teams.findOne(j + 1);
                double adjustedOppAverageGWP = computeAdjustedOppGWP(current.getOpponents());
                current.setAdjustedAverageOppGWP(adjustedOppAverageGWP);
                teams.save(current);
            }

            for (int k = 0; k < teams.count(); k++) {
                Team current = teams.findOne(k + 1);
                Team average = teams.findOne(33);
                double adjustedOppGWPOdds = current.computeOddsFromProb(current.getAdjustedAverageOppGWP());
                double adjustedOppGWPLogit = current.computeLogitFromOdds(adjustedOppGWPOdds);
                double adjustedTeamLogit = current.computeAdjustedGWPLogit(current.getTeamLogit(), average.getTeamLogit(),
                        adjustedOppGWPLogit);
                double adjustedOdds = current.computeOdds(adjustedTeamLogit);
                double adjustedGwp = current.computeProbability(adjustedOdds);
                current.setAdjustedGWP(adjustedGwp);
                teams.save(current);
            }
        }
    }

    public double computeOppGWP(ArrayList<Integer> oppList) {
        double totalOppGWP = 0;
        for (int i = 0; i < oppList.size(); i++) {

            Team oppTeam = teams.findOne(oppList.get(i));

            totalOppGWP = oppTeam.getGWP() + totalOppGWP;
        }
        return totalOppGWP/oppList.size();
    }

    public double computeAdjustedOppGWP(ArrayList<Integer> oppList) {
        double totalOppGWP = 0;
        double avgGWP = teams.findOne(33).getGWP();
        for (int i = 0; i < oppList.size(); i++) {

            Team oppTeam = teams.findOne(oppList.get(i));

            totalOppGWP = oppTeam.getAdjustedGWP() + totalOppGWP;
        }
        return totalOppGWP/oppList.size();
    }
}

