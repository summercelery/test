package com.example.service;

import com.example.dto.ContactRequest;
import com.example.entity.Contact;
import com.example.entity.Tag;
import com.example.repository.ContactRepository;
import com.example.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 提醒人服务类
 */
@Slf4j
@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private TagRepository tagRepository;

    /**
     * 创建提醒人
     */
    @Transactional
    public Contact createContact(Long userId, ContactRequest request) {
        // 检查姓名是否重复
        if (contactRepository.findByUserIdAndName(userId, request.getName()).isPresent()) {
            throw new RuntimeException("该姓名已存在");
        }

        Contact contact = new Contact();
        contact.setUserId(userId);
        contact.setName(request.getName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setWechatOpenid(request.getWechatOpenid());

        // 关联标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds())
                    .stream()
                    .filter(tag -> tag.getUserId().equals(userId))
                    .collect(Collectors.toList());
            contact.setTags(tags);
        }

        Contact savedContact = contactRepository.save(contact);
        log.info("用户 {} 创建提醒人: {}", userId, savedContact.getId());
        return savedContact;
    }

    /**
     * 获取用户的提醒人列表
     */
    public Page<Contact> getUserContacts(Long userId, Pageable pageable) {
        return contactRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取所有提醒人（不分页）
     */
    public List<Contact> getAllUserContacts(Long userId) {
        return contactRepository.findByUserId(userId);
    }

    /**
     * 根据标签获取提醒人
     */
    public List<Contact> getContactsByTag(Long userId, Long tagId) {
        return contactRepository.findByUserIdAndTagId(userId, tagId);
    }

    /**
     * 搜索提醒人
     */
    public List<Contact> searchContacts(Long userId, String name) {
        return contactRepository.findByUserIdAndNameContaining(userId, name);
    }

    /**
     * 获取提醒人详情
     */
    public Optional<Contact> getContactById(Long contactId, Long userId) {
        return contactRepository.findById(contactId)
                .filter(contact -> contact.getUserId().equals(userId));
    }

    /**
     * 更新提醒人
     */
    @Transactional
    public Contact updateContact(Long contactId, Long userId, ContactRequest request) {
        Optional<Contact> contactOpt = getContactById(contactId, userId);
        if (!contactOpt.isPresent()) {
            throw new RuntimeException("提醒人不存在");
        }

        Contact contact = contactOpt.get();
        
        // 检查姓名是否重复（排除自己）
        Optional<Contact> existingContact = contactRepository.findByUserIdAndName(userId, request.getName());
        if (existingContact.isPresent() && !existingContact.get().getId().equals(contactId)) {
            throw new RuntimeException("该姓名已存在");
        }

        contact.setName(request.getName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setWechatOpenid(request.getWechatOpenid());

        // 更新标签关联
        if (request.getTagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds())
                    .stream()
                    .filter(tag -> tag.getUserId().equals(userId))
                    .collect(Collectors.toList());
            contact.setTags(tags);
        }

        Contact updatedContact = contactRepository.save(contact);
        log.info("用户 {} 更新提醒人: {}", userId, contactId);
        return updatedContact;
    }

    /**
     * 删除提醒人
     */
    @Transactional
    public boolean deleteContact(Long contactId, Long userId) {
        Optional<Contact> contactOpt = getContactById(contactId, userId);
        if (contactOpt.isPresent()) {
            contactRepository.deleteById(contactId);
            log.info("用户 {} 删除提醒人: {}", userId, contactId);
            return true;
        }
        return false;
    }

    /**
     * 获取提醒人统计信息
     */
    public long getContactCount(Long userId) {
        return contactRepository.countByUserId(userId);
    }

    /**
     * 根据手机号查找提醒人
     */
    public Optional<Contact> findByPhoneNumber(Long userId, String phoneNumber) {
        return contactRepository.findByUserIdAndPhoneNumber(userId, phoneNumber);
    }

    /**
     * 根据微信OpenID查找提醒人
     */
    public Optional<Contact> findByWechatOpenid(Long userId, String wechatOpenid) {
        return contactRepository.findByUserIdAndWechatOpenid(userId, wechatOpenid);
    }
} 