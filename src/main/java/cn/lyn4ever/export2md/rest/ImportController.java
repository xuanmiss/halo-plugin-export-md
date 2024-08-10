package cn.lyn4ever.export2md.rest;

import cn.lyn4ever.export2md.service.ImportService;
import cn.lyn4ever.export2md.util.FileUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ApiVersion;
import run.halo.app.plugin.PluginsRootGetter;

/**
 * 自定义导入接口
 *
 * @author Lyn4ever29
 * @url https://jhacker.cn
 * @date 2023/11/1
 */
@ApiVersion("v1alpha1")
@RequestMapping("/doImport")
@RestController
@Slf4j
public class ImportController {
// /apis/api.plugin.halo.run/v1alpha1/plugins/export2doc/doImport/**


    @Autowired
    private ImportService importService;

    @Autowired
    private ReactiveExtensionClient reactiveClient;

    @Autowired
    private PluginsRootGetter pluginsRootGetter;


    @PostMapping(value = "/import1", consumes = {
        MediaType.TEXT_MARKDOWN_VALUE,
        MediaType.TEXT_EVENT_STREAM_VALUE,
        "text/*",
        MediaType.APPLICATION_PROBLEM_JSON_VALUE,
        MediaType.MULTIPART_FORM_DATA_VALUE})
    @Deprecated
    public Flux<Post> importPost1(@CookieValue("XSRF-TOKEN") String token,
        @CookieValue("SESSION") String session,
        @RequestPart("file") final Flux<FilePart> filePartFlux) {

        //保存文件
        //保存文件


        return filePartFlux.flatMap(filePart -> {
            File file =
                new File(pluginsRootGetter.get().resolve("export2doc_files").resolve(FileUtil.DirPath.IMPORT.name().toLowerCase()).toFile().getAbsolutePath()
                    + "/" + filePart.filename());
            return filePart.transferTo(file)
                .flatMap(f -> importService.runTask(file));
        });

    }

    @PostMapping(value = "/import", consumes = {
        MediaType.TEXT_MARKDOWN_VALUE,
        MediaType.TEXT_EVENT_STREAM_VALUE,
        "text/*",
        MediaType.APPLICATION_PROBLEM_JSON_VALUE,
        MediaType.MULTIPART_FORM_DATA_VALUE})
    public Flux<Post> importPost(@RequestPart("file") final Flux<FilePart> filePartFlux) {

        //保存文件
        //保存文件

        return filePartFlux.publishOn(Schedulers.boundedElastic()).flatMap(filePart -> {
            String filePath = pluginsRootGetter.get().resolve("export2doc_files").resolve(FileUtil.DirPath.IMPORT.name().toLowerCase()).resolve(filePart.filename()).toString();

            File file = new File(filePath);

            System.out.println("File path: " + filePath); // 添加输出语句
            System.out.println("File exists: " + file.exists()); // 输出文件是否存在

            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                System.out.println("Creating directory: " + parentDir.getAbsolutePath());
                boolean created = parentDir.mkdirs(); // 创建目录
                if (!created) {
                    System.out.println("Failed to create directory.");
                } else {
                    System.out.println("Directory created successfully.");
                }
            }

            filePart.transferTo(file).block();
            return importService.runTask(file);
        });

    }


//        return flux.flatMap(map->{
//            List<FilePart> fileParts = map.get("file");
//            fileParts.forEach(it->{
//                new Thread(() -> {
//                    importService.runTask(it);
//                }).start();
//            });
//            return null;
//        }).then(Mono.just("OK"));


}
