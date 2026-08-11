package com.example.spark_mart.address;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> listForCustomer(Long customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    public Address add(Long customerId, String label, String addressLine, String area, String phone) {
        return addressRepository.save(new Address(customerId, label, addressLine, area, phone));
    }

    public void delete(Long addressId, Long customerId) {
        addressRepository.findById(addressId)
                .filter(a -> a.getCustomerId().equals(customerId))
                .ifPresent(addressRepository::delete);
    }
}
