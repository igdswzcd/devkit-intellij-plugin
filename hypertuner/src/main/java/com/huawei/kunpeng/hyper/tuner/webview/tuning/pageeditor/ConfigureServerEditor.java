package com.huawei.kunpeng.hyper.tuner.webview.tuning.pageeditor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.kunpeng.hyper.tuner.common.constant.TuningIDEConstant;
import com.huawei.kunpeng.hyper.tuner.common.constant.enums.PageType;
import com.huawei.kunpeng.hyper.tuner.common.i18n.TuningI18NServer;
import com.huawei.kunpeng.hyper.tuner.common.utils.NginxUtil;
import com.huawei.kunpeng.hyper.tuner.http.TuningHttpsServer;
import com.huawei.kunpeng.hyper.tuner.toolview.dialog.impl.CompatibilityDialog;
import com.huawei.kunpeng.hyper.tuner.toolview.panel.impl.TuningConfigSuccessPanel;
import com.huawei.kunpeng.hyper.tuner.webview.TuningWebFileEditor;
import com.huawei.kunpeng.hyper.tuner.webview.tuning.pagewebview.ConfigureServerWebView;
import com.huawei.kunpeng.intellij.common.ConfigInfo;
import com.huawei.kunpeng.intellij.common.IDEContext;
import com.huawei.kunpeng.intellij.common.bean.NotificationBean;
import com.huawei.kunpeng.intellij.common.bean.RequestDataBean;
import com.huawei.kunpeng.intellij.common.bean.ResponseBean;
import com.huawei.kunpeng.intellij.common.constant.IDEConstant;
import com.huawei.kunpeng.intellij.common.constant.UserManageConstant;
import com.huawei.kunpeng.intellij.common.enums.ConfigProperty;
import com.huawei.kunpeng.intellij.common.enums.HttpMethod;
import com.huawei.kunpeng.intellij.common.enums.IDEPluginStatus;
import com.huawei.kunpeng.intellij.common.i18n.CommonI18NServer;
import com.huawei.kunpeng.intellij.common.log.Logger;
import com.huawei.kunpeng.intellij.common.util.CommonUtil;
import com.huawei.kunpeng.intellij.common.util.ConfigUtils;
import com.huawei.kunpeng.intellij.common.util.FileUtil;
import com.huawei.kunpeng.intellij.common.util.IDENotificationUtil;
import com.huawei.kunpeng.intellij.js2java.webview.pagewebview.AbstractWebView;
import com.huawei.kunpeng.intellij.ui.utils.UIUtils;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class ConfigureServerEditor extends TuningWebFileEditor {

    /**
     * saveConfig返回值
     */
    public enum SaveConfigResponse {
        SUCCESS("SUCCESS"),
        FAIL("FAIL"),
        VERSION_MISMATCH("VERSIONMISMATCH");

        private final String value;

        SaveConfigResponse(String value) {
            this.value=value;
        }

        public String value() {
            return value;
        }
    }

    private final ConfigureServerWebView configureServerWebView;

    protected static String toolName;

    private static final String SUCCESS_CODE = TuningIDEConstant.SUCCESS_CODE;

    /**
     * 查询服务器是否可用url
     */
    public static final String SERVER_STATUS_URL = "user-management/api/v2.2/users/install-info/";
    public static final String SERVER_VERSION_URL = "/user-management/api/v2.2/users/version/";

    public ConfigureServerEditor(VirtualFile file) {
        currentFile = file;
        configureServerWebView = new ConfigureServerWebView();
        this.toolName = TuningIDEConstant.TOOL_NAME_TUNING;
    }

    @Override
    public AbstractWebView getWebView() {
        return configureServerWebView;
    }

    @Override
    @NotNull
    public JComponent getComponent() {
        return configureServerWebView.getContent();
    }

    @Override
    public void dispose() {
        super.dispose();
        configureServerWebView.dispose();
    }

    public static void openPage() {
        System.out.println("opening page configure server");
        String fileName = TuningIDEConstant.TUNING_KPHT +
                IDEConstant.PATH_SEPARATOR +
                PageType.CONFIGURE_SERVER.value() +
                IDEConstant.PATH_SEPARATOR +
                TuningI18NServer.toLocale("plugins_hyper_tuner_lefttree_server_config_now") +
                "." +
                TuningIDEConstant.TUNING_KPHT;

        closeWebView(fileName);
        openWebView(fileName);
    }

    /**
     * 配置服务器信息回传后
     */
    public String saveConfig(Map<String, String> params) {
        preSaveConfig();
        if (!save(params)) {
            // 配置服务器失败
            System.out.println("saving config failed!!!");
            IDENotificationUtil.notificationCommon(new NotificationBean(
                    UserManageConstant.CONFIG_TITLE,
                    TuningI18NServer.toLocale("plugins_common_message_responseError_messagePrefix"),
                    NotificationType.ERROR
            ));
            return SaveConfigResponse.FAIL.value();
        } else {
            // 配置服务器成功
            // 判断服务器兼容性
            boolean isCompatible = checkServiceVersionCompatible();
            if (isCompatible) {
                // 仅在版本适配的情况下打开 web view 页面，允许用户使用
                System.out.println("is compatible!!");
//                ApplicationManager.getApplication().invokeLater(() -> {
//                    NginxUtil.updateNginxConfig(params.get("ip"), params.get("port"), params.get("localPort"));
//                    // 打开web首页
//                    IDELoginEditor.openPage(params.get("localPort"));
//                });
//                NginxUtil.updateNginxConfig(params.get("ip"), params.get("port"), params.get("localPort"));
//                IDELoginEditor.openPage(params.get("localPort"));
                return SaveConfigResponse.SUCCESS.value();
            }
            return SaveConfigResponse.VERSION_MISMATCH.value();
        }
    }

    /**
     * 保存服务器配置
     *
     * @param params ip port certFile证书文件 useCertFlag是否使用证书
     * @return true:保存服务器配置成功
     */
    private boolean save(Map<String, String> params) {
        String host = params.get("ip");
        String port = params.get("port");
        String localPort = params.get("localPort");
        ConfigUtils.fillIp2JsonFile(toolName, host, port, localPort);
        // 将plugin设置为配置服务器状态
        IDEContext.setIDEPluginStatus(toolName, IDEPluginStatus.IDE_STATUS_SERVER_DEPLOY);
        // check connection is ok
        ResponseBean response = getServiceConfigResponse();
        if (response != null &&
                (SUCCESS_CODE.equals(response.getCode()) || SUCCESS_CODE.equals(response.getStatus()))) {
//            if (!HttpsServer.isCertConfirm && Boolean.parseBoolean(params.get("useCertFlag"))) {
//                serverCertConfirmFailed(toolName, host);
//                return false;
//            }
//            showNotification();
            // update global Context
            updateIDEContext(host);
            // clear userConfig when config server again
            ConfigUtils.fillIp2JsonFile(toolName, params.get("ip"), params.get("port"), params.get("localPort"));
            ConfigUtils.updateUserConfig(ConfigProperty.AUTO_LOGIN_CONFIG.vaLue(), " ", false, false);
            synchronizedLeftTree();
            return true;
        }
        // 将plugin设置为初始状态
        IDEContext.setIDEPluginStatus(toolName, IDEPluginStatus.IDE_STATUS_INIT);
        // 清空本地 ip 缓存
        ConfigUtils.fillIp2JsonFile(toolName, "", "", "");
        return false;
    }

    /**
     * 配置服务器成功后提示携带登录跳转
     */
    private void showNotification() {
        Project project = CommonUtil.getDefaultProject();
        String content = CommonI18NServer.toLocale("common_config_success");
        NotificationBean notificationBean = new NotificationBean(CommonI18NServer.toLocale(
                "common_config_title"), content, NotificationType.INFORMATION);
        notificationBean.setProject(project);
//        IDENotificationUtil.notificationForHyperlink(notificationBean, data -> notificationForHyperlinkAction());
        IDENotificationUtil.notificationCommon(notificationBean);
    }
//
    private void synchronizedLeftTree() {
        // 如果打开多个project， 同步每一个project左侧树状态
        Project[] openProjects = ProjectUtil.getOpenProjects();
        ApplicationManager.getApplication().invokeLater(() -> {
            for (Project proj : openProjects) {
                // 配置服务器完成后刷新左侧树面板为已配置面板
                customizeRefreshPanel(proj);
            }
        });
    }

    /**
     * 首次登录时判断修改的IP和端口是否可正常访问
     *
     * @return ResponseBean 响应实体
     */
    protected ResponseBean getServiceConfigResponse() {
        RequestDataBean message = new RequestDataBean(TuningIDEConstant.TOOL_NAME_TUNING, SERVER_STATUS_URL,
                HttpMethod.GET.vaLue(), false);
        return TuningHttpsServer.INSTANCE.requestData(message);
    }

    /**
     * 更新缓存：当前插件状态以及
     *
     * @param ip 配置服务器地址
     */
    private void updateIDEContext(String ip) {
        // update globe IDEPluginStatus
        IDEContext.setIDEPluginStatus(toolName, IDEPluginStatus.IDE_STATUS_SERVER_CONFIG);
        ConfigInfo curInfo = new ConfigInfo(ip, "");
        IDEContext.getProjectConfig().put(CommonUtil.getDefaultProject().getName() + "#" + toolName, curInfo);
    }

    /**
     * 配置服务器完成后刷新左侧树面板为已配置面板
     *
     * @param proj openProjects
     */
    protected void customizeRefreshPanel(Project proj) {
        ToolWindow toolWindow =
                ToolWindowManager.getInstance(proj).getToolWindow(TuningIDEConstant.HYPER_TUNER_TOOL_WINDOW_ID);
        TuningConfigSuccessPanel tuningConfigSuccessPanel = new TuningConfigSuccessPanel(toolWindow, proj);
        if (toolWindow != null) {
            toolWindow.getContentManager().addContent(tuningConfigSuccessPanel.getContent());
            toolWindow.getContentManager().setSelectedContent(tuningConfigSuccessPanel.getContent());
        }
    }

    protected void preSaveConfig() {
        // 左侧树面板加载loading，loadingText为系统默认
        UIUtils.changeToolWindowToLoadingPanel(CommonUtil.getDefaultProject(), null,
                TuningIDEConstant.HYPER_TUNER_TOOL_WINDOW_ID);
    }

    /**
     * 通过调用接口获取服务端版本，读取本地配置文件中的兼容性配置字段，判断是否当前版本插件是否支持
     *
     * @return boolean true：兼容当前服务端版本，false：不兼容当前服务端版本，并在右下角提示弹窗
     */
    protected boolean checkServiceVersionCompatible() {
        RequestDataBean message = new RequestDataBean(TuningIDEConstant.TOOL_NAME_TUNING, SERVER_VERSION_URL,
                HttpMethod.GET.vaLue(), false);
        ResponseBean responseBean = TuningHttpsServer.INSTANCE.requestData(message);
        if (responseBean == null) {
            Logger.warn("An error occurred while getting the server version, the response is null");
            return false;
        }
        String responseBeanDataJsStr = responseBean.getData();
        JSONObject jsonObject = JSON.parseObject(responseBeanDataJsStr);
        String serverVersionStr = jsonObject.getString("version");

        boolean isContains = true; // 默认插件兼容所有版本插件
        Map config = FileUtil.ConfigParser.parseJsonConfigFromFile(IDEConstant.CONFIG_PATH);
        Object configVersionObj = config.get(ConfigProperty.CONFIG_VERSION.vaLue());
        String minimumVersion = "";
        if (configVersionObj instanceof List) {
            List configList = (List) configVersionObj;
            if (!configList.isEmpty()) {
                // 配置文件中兼容性版本不为空，则说明对兼容性有要求
                isContains = configList.contains(serverVersionStr);
                minimumVersion = configList.get(0) + "";
            } else {
                Logger.warn("Plugin compatibility is not configured, all background version are compatible by default");
            }
        }
        Logger.info("The current plugin version compatibility is " + isContains);
        if (!isContains) {
            String serverOldTip = MessageFormat.format(
                    TuningI18NServer.toLocale("plugins_hyper_tuner_version_server_old"),
                    minimumVersion, serverVersionStr);
            String title = TuningI18NServer.toLocale("plugins_hyper_tuner_version_tip");
            CompatibilityDialog dialog = new CompatibilityDialog(title, serverOldTip);
            dialog.displayPanel();
        }
        return isContains;
    }
}
