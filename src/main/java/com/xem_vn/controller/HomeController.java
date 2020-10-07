package com.xem_vn.controller;

import com.xem_vn.model.AppRole;
import com.xem_vn.model.AppUser;
import com.xem_vn.model.Post;
import com.xem_vn.model.Status;
import com.xem_vn.service.IAppRoleService;
import com.xem_vn.service.IAppUserService;
import com.xem_vn.service.IPostService;
import com.xem_vn.service.IStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;

@Controller
public class HomeController {
    @Autowired
    IAppRoleService roleService;

    @Autowired
    IPostService postService;

    @Value("${upload.path}")
    private String upload_path;
    @Autowired
    private IAppUserService appUserService;

    @Autowired
    private IAppUserService userService;

    @Autowired
    IStatusService statusService;

    @ModelAttribute("user")
    private AppUser getPrincipal() {
        AppUser appUser = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            appUser = userService.getUserByUserName(((UserDetails) principal).getUsername()).orElse(null);
        }
        return appUser;
    }

    @GetMapping({"/", "/home"})
    public ModelAndView showApprovalPage(@PageableDefault(value = 10, page = 0)
                                         @SortDefault(sort = "dateUpload", direction = Sort.Direction.DESC)
                                                 Pageable pageable) {
        ModelAndView modelAndView = new ModelAndView("/welcome");
        Status status = statusService.findByName("approve").get();
        Page<Post> postPage =  postService.getAllPostByStatus(status, pageable);
        modelAndView.addObject("posts", postPage);
        modelAndView.addObject("currentTime", System.currentTimeMillis());
        return modelAndView;
    }

    @GetMapping("/Access_Denied")
    public String accessDeniedPage(ModelMap model) {
        model.addAttribute("user", getPrincipal().getUsername());
        return "accessDenied";
    }

    @GetMapping("/create-account")
    public ModelAndView showCreateUserForm(){
        ModelAndView modelAndView = new ModelAndView("account/create");
        modelAndView.addObject("newUser",new AppUser());
        return modelAndView;
    }

    @PostMapping("/create-account")
    public ModelAndView createUser(@ModelAttribute("newUser") AppUser user){
        System.out.println("post create : " + user);
        System.out.println(user.getUsername());
        AppRole role = roleService.getRoleByName("ROLE_USER");
        user.setRole(role);
        MultipartFile avatar = user.getAvatarFile();
        String avatarFileName = avatar.getOriginalFilename();
        user.setAvatarFileName(avatarFileName);
        userService.save(user);
        try {
            FileCopyUtils.copy(avatar.getBytes(), new File(upload_path + avatarFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  new ModelAndView("/account/create");
    }

    @GetMapping("/uploader/{id}")
    public ModelAndView showUploaderPage(@PathVariable("id") Long userId,
                                         @PageableDefault(value = 10, page = 0)
                                         @SortDefault(sort = "date_Upload", direction = Sort.Direction.DESC)
                                                 Pageable pageable) {
        AppUser user = userService.getUserById(userId);
        Page<Post> posts = postService.getAllPostByUser(user, pageable);
        ModelAndView modelAndView = new ModelAndView("/account/uploader");
        modelAndView.addObject("posts", posts);
        return modelAndView;
    }

}
