package com.yeahmobi.loadbalance_manager.api;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.adclear.dbunit.json.annotations.JsonData;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.yeahmobi.loadbalance_manager.api.AbstractController.JsonResult;
import com.yeahmobi.loadbalance_manager.common.Constants;
import com.yeahmobi.loadbalance_manager.model.Vhost;

@SuppressWarnings("unchecked")
public class VhostControllerTest extends ControllerBaseTest {

    @Autowired
    private VhostController vhostController;

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testList() throws Exception {
        String json = this.vhostController.list();
        JsonResult<List<Vhost>> actualResult = JSON.parseObject(json, new TypeReference<JsonResult<List<Vhost>>>() {
        });

        JsonResult expectedResult = createSuccessJsonResult();
        Vhost vhostA = createVhost("testVhostA");
        Vhost vhostB = createVhost("testVhostB");
        expectedResult.setResult(Lists.newArrayList(vhostA, vhostB));

        ReflectionAssert.assertReflectionEquals(expectedResult, actualResult, ReflectionComparatorMode.LENIENT_DATES);
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testGet() throws Exception {
        String[] keys = { "testVhostA" };
        String json = this.vhostController.get(keys);
        JsonResult<List<Vhost>> actualResult = JSON.parseObject(json, new TypeReference<JsonResult<List<Vhost>>>() {
        });

        JsonResult expectedResult = createSuccessJsonResult();
        Vhost vhostA = createVhost("testVhostA");
        expectedResult.setResult(Lists.newArrayList(vhostA));

        assertJsonResult(expectedResult, actualResult);
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testSaveAsInsert() throws Exception {
        // new & insert
        Vhost vhost = createVhost("testVhostC");
        vhost.setVersion(null);
        this.vhostController.save(vhost);

        // get & test
        String[] keys = { vhost.getName() };
        String json = this.vhostController.get(keys);
        JsonResult<List<Vhost>> actualResult = JSON.parseObject(json, new TypeReference<JsonResult<List<Vhost>>>() {
        });

        // expect
        JsonResult expectedResult = createSuccessJsonResult();
        vhost.setVersion(1L);// version will inc
        expectedResult.setResult(Lists.newArrayList(vhost));

        assertJsonResult(expectedResult, actualResult);
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testSaveAsUpdate() throws Exception {
        // update
        Vhost vhost = createVhost("testVhostA");
        vhost.setCustom("custom2");
        this.vhostController.save(vhost);

        // get & test
        String[] keys = { vhost.getName() };
        String json = this.vhostController.get(keys);
        JsonResult<List<Vhost>> actualResult = JSON.parseObject(json, new TypeReference<JsonResult<List<Vhost>>>() {
        });

        // expect
        JsonResult expectedResult = createSuccessJsonResult();
        vhost.setVersion(2L);// version will inc
        List<Vhost> list = Lists.newArrayList(vhost);
        expectedResult.setResult(list);

        assertJsonResult(expectedResult, actualResult);
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testDel() throws Exception {
        // del
        String[] keys = { "testVhostA" };
        this.vhostController.del(keys);

        // get
        String json = this.vhostController.get(keys);
        JsonResult<String> actualResult = JSON.parseObject(json, new TypeReference<JsonResult<String>>() {
        });

        // expect
        JsonResult expectedResult = createFailJsonResult();

        Assert.assertEquals(expectedResult.getErrorCode(), actualResult.getErrorCode());
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testInfoRegion() throws Exception {
        // save & configGen will auto insert
        Vhost vhost = createVhost("testVhostA");
        vhost.setCustom("custom2");
        this.vhostController.save(vhost);
        vhost = createVhost("testVhostB");
        vhost.setCustom("custom2");
        this.vhostController.save(vhost);

        String json = this.vhostController.info();

        JsonResult<List<Map<String, Object>>> actualResult = JSON.parseObject(json,
                                                                              new TypeReference<JsonResult<List<Map<String, Object>>>>() {
                                                                              });

        assertInfo(actualResult);

    }

    private void assertInfo(JsonResult<List<Map<String, Object>>> actualResult) throws ParseException {
        List<Map<String, Object>> list = actualResult.getResult();
        Assert.assertEquals(2, list.size());
        {
            Map<String, Object> map = list.get(0);
            Assert.assertEquals("testVhostA", map.get("vhost"));
        }
        {
            Map<String, Object> map = list.get(1);
            Assert.assertEquals("testVhostB", map.get("vhost"));
        }
    }

    @Test
    @JsonData(fileName = "initVhost.json")
    public void testDownload() throws Exception {
        // save & configGen will auto insert
        Vhost vhost = createVhost("testVhostA");
        vhost.setCustom("custom2");
        this.vhostController.save(vhost);

        HttpEntity<String> entity = (HttpEntity<String>) this.vhostController.download(1L);

        System.out.println(entity);

        Assert.assertEquals(StringUtils.trim(IOUtils.toString(GlobalControllerTest.class.getResourceAsStream("/download/vhost.conf"))),
                            StringUtils.trim(entity.getBody()));
    }

    private void assertJsonResult(JsonResult expectedResult, JsonResult actualResult) {
        Assert.assertEquals(expectedResult.getErrorCode(), actualResult.getErrorCode());
        Assert.assertEquals(expectedResult.getMsg(), actualResult.getMsg());

        ReflectionAssert.assertReflectionEquals(expectedResult.getResult(), actualResult.getResult(),
                                                ReflectionComparatorMode.LENIENT_DATES);
    }

    @Override
    protected JsonResult createSuccessJsonResult() {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setErrorCode(JsonResult.CODE_SUCCESS);
        jsonResult.setMsg("success");
        return jsonResult;
    }

    protected Vhost createVhost(String name) throws ParseException {
        Vhost vhost = new Vhost();
        vhost.setName(name);
        vhost.setCustom("custom");
        vhost.setIsDefaultServer(false);
        vhost.setListenPort(9090);
        List<String> serverNames = Lists.newArrayList("serverA", "serverB", "serverC");
        vhost.setServerNames(serverNames);
        vhost.setUpstreamName("testUpstreamA");
        vhost.setVersion(1L);
        vhost.setGmtCreate(DateUtils.parseDate("2014-12-15 08:59:09", Constants.DATE_PATTERNS));
        vhost.setGmtModified(DateUtils.parseDate("2014-12-16 08:59:09", Constants.DATE_PATTERNS));
        return vhost;
    }

}
