# 🛡️ Access Governance Hub

> **An Enterprise Identity Governance & Administration (IGA) Platform built using Microservices, Event-Driven Architecture, and Workflow Automation.**

---

# Project Vision

The goal of this project is to build an enterprise-grade **Identity Governance & Administration (IGA)** platform inspired by products like SailPoint, Saviynt, Microsoft Entra ID Governance, and Oracle Identity Governance.

Rather than being a simple RBAC implementation, this project focuses on the complete lifecycle of enterprise access management.

The platform answers five fundamental questions:

* **Who** is requesting access?
* **What** resource is being requested?
* **Why** is the access required?
* **Who** should approve it?
* **When** should that access be granted or revoked?

---

# The Problem

Imagine a company with 25,000 employees.

Every employee may require access to:

* GitHub
* Jira
* Confluence
* AWS
* Azure
* Production Servers
* VPN
* HR Systems
* Finance Systems
* Databases
* Internal APIs

Now consider what happens every day:

* New employees join.
* Existing employees change departments.
* Contractors join for only two months.
* Employees resign.
* Teams need temporary production access.
* Developers require elevated database permissions.
* Vendors need limited access to internal portals.

Without governance:

* Access is requested through emails.
* Managers forget to revoke permissions.
* Employees accumulate unnecessary privileges.
* Compliance teams cannot determine who approved access.
* Auditors cannot trace historical changes.
* Insider security risks increase significantly.

---

# Solution

Access Governance Hub introduces a centralized workflow-driven platform for managing access requests across the organization.

Instead of sending emails or manually updating permissions, users request access through the portal.

The system automatically:

* Validates policies
* Finds the correct approvers
* Executes configurable approval workflows
* Provisions access
* Sends notifications
* Maintains complete audit history
* Supports periodic access reviews

Every action is tracked from request creation until access revocation.

---

# Core Objectives

The project aims to provide:

* Centralized Identity Governance
* Role-Based Access Control (RBAC)
* Policy-Based Authorization
* Dynamic Approval Workflows
* Automated Access Provisioning
* Event-Driven Communication
* Audit & Compliance
* Access Reviews
* Separation of Duties (SoD)
* Least Privilege Enforcement

---

# Key Features

## Identity Management

* Employee Directory
* Departments
* Business Units
* Managers
* Roles
* Permission Catalog
* User Groups

---

## Authentication & Authorization

* JWT Authentication
* Refresh Tokens
* Role-Based Access Control
* Permission-Based Authorization
* API Gateway Security
* Redis Permission Caching

---

## Access Requests

Users can request access to:

* Applications
* Roles
* Individual Permissions
* Production Environments
* Databases
* Cloud Resources
* Shared Resources

Each request contains:

* Business Justification
* Duration
* Priority
* Risk Level
* Attachments
* Expiration Date

---

## Dynamic Approval Engine

Unlike traditional systems with hardcoded approvals, workflows are completely configurable.

Supported strategies include:

* ANY_ONE_APPROVES
* ALL_MUST_APPROVE
* MINIMUM_N_APPROVERS

Approvers can be selected based on:

* Manager Hierarchy
* Department
* Business Unit
* Resource Owner
* Security Team
* Compliance Team
* Custom Rules

---

## Policy Engine

Before approval, policies are evaluated automatically.

Examples:

* Developers cannot approve their own production access.
* Contractors cannot receive permanent permissions.
* Finance permissions require CFO approval.
* Production access expires after 24 hours.
* High-risk requests require Security approval.

---
## Audit Trail

Every action is recorded.

Examples:

* Request Created
* Workflow Started
* Approval Granted
* Approval Rejected
* Permission Assigned
* Permission Revoked
* Policy Violations
* Login Activity

This provides complete traceability for compliance audits.

---

## Notifications

Users receive notifications for:

* Request Submitted
* Approval Pending
* Approved
* Rejected
* Provisioned
* Expiring Access
* Access Review

Delivery channels:

* Email
* In-App Notifications
* Kafka Events
* Future Support:

  * Slack
  * Microsoft Teams

---

# High-Level Architecture

```
                         +----------------------+
                         |     React Frontend   |
                         +----------+-----------+
                                    |
                             API Gateway
                                    |
     ------------------------------------------------------------
     |        |         |          |         |         |          |
     |        |         |          |         |         |          |
 Auth     User     Request    Workflow   Policy   Notification  Audit
Service  Service   Service     Service    Engine      Service    Service
     |        |         |          |         |         |          |
     --------------------- Kafka Event Bus ------------------------
                             |
                     Provisioning Service
                             |
                External Applications / IAM
```

---

# Technology Stack

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

## Authentication

* JWT
* Refresh Tokens
* BCrypt

## Databases

* PostgreSQL
* Redis

## Event Streaming

* Apache Kafka

## API

* REST APIs
* OpenAPI / Swagger

## Infrastructure

* Docker
* Docker Compose

## Frontend

* React
* TypeScript
* Tailwind CSS

## DevOps (Planned)

* Kubernetes
* GitHub Actions
* Prometheus
* Grafana

---

# Microservices

* API Gateway
* Authentication Service
* User Service
* Role & Permission Service
* Access Request Service
* Workflow Service
* Policy Engine
* Provisioning Service
* Notification Service
* Audit Service

Each service owns its own database following the Database-per-Service pattern.

---

# Event-Driven Architecture

Kafka is used for asynchronous communication.

Example events:

```
AccessRequested

↓

WorkflowStarted

↓

ApprovalCompleted

↓

ProvisioningStarted

↓

AccessGranted

↓

NotificationSent

↓

AuditLogged
```

This keeps services loosely coupled and independently scalable.

---

# Design Principles

The project follows modern software architecture principles:

* Microservices Architecture
* Event-Driven Design
* Domain-Driven Design (DDD)
* SOLID Principles
* Database Per Service
* Asynchronous Communication
* Idempotent Consumers
* Stateless Services
* Secure by Default

---

# Future Roadmap

* Attribute-Based Access Control (ABAC)
* Risk-Based Access Decisions
* AI-powered Access Recommendations
* Identity Analytics
* Privileged Access Management (PAM)
* SCIM Provisioning
* SAML & OAuth2 Federation
* Multi-Tenant Support
* Workflow Designer (Drag & Drop)
* Webhooks
* Terraform Integration
* Kubernetes Operator
* Mobile Application

---

# Learning Goals

This project demonstrates practical implementation of:

* Enterprise Authentication
* Authorization Systems
* Identity Governance
* RBAC & Policy Engines
* Workflow Engines
* Event-Driven Microservices
* Distributed Systems
* Kafka Messaging
* Secure API Design
* Audit Logging
* Distributed Transactions
* Scalable Backend Architecture

---

# Why This Project?

Many portfolio projects stop at implementing login and role-based authentication.

Enterprise systems require much more.

This project explores how large organizations manage identity, permissions, approvals, compliance, and security at scale. It combines authentication, authorization, workflow automation, distributed systems, event-driven communication, and governance into a single cohesive platform that reflects real-world enterprise architecture.

The objective is not just to build another CRUD application, but to understand and implement the engineering patterns behind enterprise Identity Governance & Administration (IGA) platforms used by Fortune 500 organizations.
