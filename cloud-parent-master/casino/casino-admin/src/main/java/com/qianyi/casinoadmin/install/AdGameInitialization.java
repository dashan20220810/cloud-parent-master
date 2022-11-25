package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.ExcelUtils;
import com.qianyi.casinocore.model.AdGame;
import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.service.AdGamesService;
import com.qianyi.casinocore.service.PlatformGameService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@Order(5)
public class AdGameInitialization implements CommandLineRunner {

    @Autowired
    private AdGamesService adGamesService;

    @Autowired
    private PlatformGameService platformGameService;

    @Override
    public void run(String... args) throws Exception {
        addPlatformGame();

        long count = adGamesService.fontCount();
        if(count > 0){
            return;
        }
        InputStream inputStream = this.getClass().getResourceAsStream("/excleTemplate/gameList.xlsx");
        XSSFWorkbook xssfSheets = new XSSFWorkbook(inputStream);
        List<List<String>> lists = ExcelUtils.readExcelContentList(xssfSheets);
        List<AdGame> adGameList = new ArrayList<>();
        for (List<String> list : lists) {
            AdGame adGame = new AdGame();
            for (int i = 0; i < list.size(); i++) {
                if(i == 1){
                    adGame.setGameName(list.get(i).trim());
                }
                if(i == 2){
                    adGame.setGameCode(list.get(i).trim());
                }
                if(i == 3){
                    adGame.setGamesStatus(Integer.parseInt(list.get(i)));
                }
                if(i == 4){
                    adGame.setGameEnName(list.get(i).trim());
                }
                if(i == 5){
                    adGame.setGamePlatformName(list.get(i).trim());
                }
                adGameList.add(adGame);
            }
        }
        adGamesService.saveAll(adGameList);
    }

    /**
     * 添加平台
     */
    private void addPlatformGame() {
        List<PlatformGame> platformGameList = platformGameService.findAll();

        if(platformGameList == null || platformGameList.isEmpty()){
            List<PlatformGame> platformGames = new ArrayList<>();
            platformGames.add(new PlatformGame("WM", 1));
            platformGames.add(new PlatformGame("PG", 1));
            platformGames.add(new PlatformGame("CQ9", 1));
            platformGames.add(new PlatformGame("OB", 1));
            platformGameService.saveAll(platformGames);
        }


        List<PlatformGame> platforms = new ArrayList<>();
        List<String> collect = platformGameList.stream().map(PlatformGame::getGamePlatformName).collect(Collectors.toList());
        if(!collect.contains("OB")) {
            platforms.add(new PlatformGame("OB", 1));
            platformGameService.saveAll(platforms);
            AdGame obdjGame = new AdGame("OB", "OBDJ", "欧博电竞", "OBDJ", 1);
            AdGame obtyGame = new AdGame("OB", "OBTY", "欧博体育", "OBTY", 1);
            List<AdGame> adGameList = new ArrayList<>();
            adGameList.add(obdjGame);
            adGameList.add(obtyGame);
            adGamesService.saveAll(adGameList);

        }
        if(!collect.contains("SABASPORT")) {
            platforms.add(new PlatformGame("SABASPORT", 2));
            platformGameService.saveAll(platforms);
            AdGame sbpcGame = new AdGame("SABASPORT", "sabasport_pc", "SABA体育(PC)", "SABASPORT(PC)", 1);
            AdGame sbh5Game = new AdGame("SABASPORT", "sabasport_h5", "SABA体育(H5)", "SABASPORT(H5)", 1);
            List<AdGame> adGameList = new ArrayList<>();
            adGameList.add(sbpcGame);
            adGameList.add(sbh5Game);
            adGamesService.saveAll(adGameList);

        }

    }
}
