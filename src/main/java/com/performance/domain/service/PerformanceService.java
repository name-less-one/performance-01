package com.performance.domain.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.performance.domain.dao.UserInfoDao;
import com.performance.domain.entity.UserInfo;

@Service
public class PerformanceService {

    final static Logger log = LogManager.getLogger(PerformanceService.class);

    private final String MEASURE_FLAG_ON  = "1";

    private GoogleApiService googleService;

    private UserInfoDao userInfoDao;
    
    private Map<String, Long> resultMap = new HashMap<String, Long>();
    private Map<String, Boolean> assertionResultMap = new HashMap<String, Boolean>();

    public PerformanceService(GoogleApiService googleService, UserInfoDao userInfoDao) {
        this.googleService = googleService;
        this.userInfoDao = userInfoDao;
    }

    @Async("perfomanceExecutor")
    public void execute(String uuid, String measureFlag) {

        resultMap.clear();
        resultMap.put(uuid, null);

        truncateTable();

        Long start = System.currentTimeMillis();

        List<UserInfo> matchingUserList = uploadExecute();

        Long end = System.currentTimeMillis();
        Long executeTime = end - start;

        resultMap.put(uuid, executeTime);
        // アサーション入れる
        Boolean assertionResult = assertion(matchingUserList);
        assertionResultMap.put(uuid, assertionResult);
        
        if(MEASURE_FLAG_ON.equals(measureFlag)) {
            try {
                googleService.execute(executeTime);
            } catch (Exception e) {
                log.error("スプレッドシートの更新でエラーが発生しました。", e);
            }
        }
        return;
    }
    public List<UserInfo> uploadExecute() {
        // CSVを取得・CSVファイルをDBに登録する
        //ファイル読み込みで使用する3つのクラス
        FileReader fr = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            fr = new FileReader(new File("data/userInfo.csv"), Charset.forName("SJIS"));
            br = new BufferedReader(fr);

            //読み込み行
            String readLine;

            //読み込み行数の管理
            int i = 0;

            //1行ずつ読み込みを行う
            while ((readLine = br.readLine()) != null) {
                i++;
                //データ内容をコンソールに表示する
                log.info("-------------------------------");

                //データ件数を表示
                log.info("データ読み込み" + i + "件目");
                
                csvFile.add(readLine);
            }
        } catch (Exception e) {
            log.info("csv read error", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }

        try {
            int i = 0;
            for(String line : csvFile) {
                //カンマで分割した内容を配列に格納する
                String[] data = line.split(",", -1);
                
                //データ内容をコンソールに表示する
                log.info("-------------------------------");
                //データ件数を表示
                //配列の中身を順位表示する。列数(=列名を格納した配列の要素数)分繰り返す
                log.debug("ユーザー姓:" + data[1]);
                log.debug("出身都道府県:" + data[2]);
                log.debug("ユーザー名:" + data[0]);
                log.debug("出身市区町村:" + data[3]);
                log.debug("血液型:" + data[4]);
                log.debug("趣味1:" + data[5]);
                log.debug("趣味2:" + data[6]);
                log.debug("趣味3:" + data[7]);
                log.debug("趣味4:" + data[8]);
                log.debug("趣味5:" + data[9]);
                UserInfo userInfo = new UserInfo();

                userInfo.setLastName(data[0]);
                userInfo.setFirstName(data[1]);
                userInfo.setPrefectures(data[2]);
                userInfo.setCity(data[3]);
                userInfo.setBloodType(data[4]);
                userInfo.setHobby1(data[5]);
                userInfo.setHobby2(data[6]);
                userInfo.setHobby3(data[7]);
                userInfo.setHobby4(data[8]);
                userInfo.setHobby5(data[9]);
                // 特定の件のみインサートするようにする
                if("新潟県上越市".equals(userInfo.getPrefectures() + userInfo.getCity())) {
                    // 行数のインクリメント
                    i++;
                    log.info("データ書き込み" + i + "件目");
                    userInfoDao.insert(userInfo);
                }
            }

        } catch (Exception e) {
            log.info("csv read error", e);
        }
        
        UserInfo targetUser = userInfoDao.getTargetUser();
        
        // DBから検索する
        List<UserInfo> userInfoList = userInfoDao.search();
        
        List<UserInfo> bloodMatchingUserList = new ArrayList<UserInfo>();
        // 同じ血液型ユーザー
        for(UserInfo user : userInfoList) {
            if(user.getBloodType().equals(targetUser.getBloodType())) {
                bloodMatchingUserList.add(user);
            }
        }
        List<UserInfo> matchingUserList = new ArrayList<UserInfo>();
        // 趣味1に同じ趣味を持っているユーザー
        for(UserInfo user : bloodMatchingUserList) {
            if(user.getHobby1().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5())) {
                if(!matchingUserList.contains(user)) {
                    matchingUserList.add(user);
                }
            }
        }
        // 趣味2に同じ趣味を持っているユーザー
        for(UserInfo user : bloodMatchingUserList) {
            if(user.getHobby2().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5())) {
                if(!matchingUserList.contains(user)) {
                    matchingUserList.add(user);
                }
            }
        }
        // 趣味3に同じ趣味を持っているユーザー
        for(UserInfo user : bloodMatchingUserList) {
            if(user.getHobby3().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5())) {
                if(!matchingUserList.contains(user)) {
                    matchingUserList.add(user);
                }
            }
        }
        // 趣味4に同じ趣味を持っているユーザー
        for(UserInfo user : bloodMatchingUserList) {
            if(user.getHobby4().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5())) {
                if(!matchingUserList.contains(user)) {
                    matchingUserList.add(user);
                }
            }
        }
        // 趣味5に同じ趣味を持っているユーザー
        for(UserInfo user : bloodMatchingUserList) {
            if(user.getHobby5().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5())) {
                if(!matchingUserList.contains(user)) {
                    matchingUserList.add(user);
                }
            }
        }
        return matchingUserList;
    }

    
    public void truncateTable() {
        userInfoDao.truncate();
    }

    public Long referenceExecuteTime(String uuid) {
        
        Long result = null;
        if(resultMap.containsKey(uuid)) {
            result = resultMap.get(uuid);
        }
        
        return result;
    }
    
    public String referenceUuid() {
        
        String uuid = null;
        
        for(String key : resultMap.keySet()) {
            uuid = key;
        }
        
        return uuid;
    }

    private Boolean assertion(List<UserInfo> matchingUserList) {
        Boolean assertionResult = true;
        
        int count = userInfoDao.searchCount();
        
        if(count != 10000) {
            return false;
        }
        
        // CSVを取得・CSVファイルをDBに登録する
        //ファイル読み込みで使用する3つのクラス
        FileReader fr = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            fr = new FileReader(new File("data/assertionData.csv"));
            br = new BufferedReader(fr);

            //読み込み行
            String readLine;
            //1行ずつ読み込みを行う
            while ((readLine = br.readLine()) != null) {
                csvFile.add(readLine);
            }
        } catch (Exception e) {
            log.info("csv read error", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        for(String line : csvFile) {
            boolean exsits = false;
            UserInfo userInfo = new UserInfo();
            String[] data = line.split(",", -1);

            userInfo.setLastName(data[0]);
            userInfo.setFirstName(data[1]);
            userInfo.setPrefectures(data[2]);
            userInfo.setCity(data[3]);
            userInfo.setBloodType(data[4]);
            userInfo.setHobby1(data[5]);
            userInfo.setHobby2(data[6]);
            userInfo.setHobby3(data[7]);
            userInfo.setHobby4(data[8]);
            userInfo.setHobby5(data[9]);
            for(UserInfo user : matchingUserList) {
                if(user.toString().equals(userInfo.toString())) {
                    exsits = true;
                    break;
                }
            }
            if(!exsits) {
                assertionResult = false;
            }
        }
        return assertionResult;
    }

    public Boolean referenceAssertionResult(String uuid) {
        Boolean assertionResult = assertionResultMap.get(uuid);
        return assertionResult;
    }
}
