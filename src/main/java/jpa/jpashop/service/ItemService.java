package jpa.jpashop.service;

import jpa.jpashop.domain.item.Item;
import jpa.jpashop.dto.ItemUpdateDto;
import jpa.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Item findItem(Long id) {
        return itemRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void updateItem(Long itemId, ItemUpdateDto updateParam) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        item.update(updateParam.getName(), updateParam.getPrice(), updateParam.getStock());
    }

    @Transactional(readOnly = true)
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

}
