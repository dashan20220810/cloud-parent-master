package com.qianyi.paycore.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "`order`")
public class Order extends BaseEntity {

}
