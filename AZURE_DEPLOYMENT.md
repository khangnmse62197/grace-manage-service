# Azure App Service Deployment Guide

This guide explains how to deploy the Grace Manage Service to Azure App Service using **Azure Portal**.

## Prerequisites

1. Azure subscription
2. GitHub repository for CI/CD

---

## Step 1: Create Resource Group

1. Go to [Azure Portal](https://portal.azure.com)
2. Search for **"Resource groups"** in the top search bar
3. Click **"+ Create"**
4. Fill in:
   - **Subscription**: Select your subscription
   - **Resource group**: `grace-rg`
   - **Region**: `East US` (or your preferred region)
5. Click **"Review + create"** → **"Create"**

---

## Step 2: Create App Service Plan

1. Search for **"App Service plans"** in the top search bar
2. Click **"+ Create"**
3. Fill in:
   - **Subscription**: Select your subscription
   - **Resource group**: `grace-rg`
   - **Name**: `grace-plan`
   - **Operating System**: `Linux`
   - **Region**: `East US` (same as resource group)
   - **Pricing plan**: Click **"Explore pricing plans"** → Select **"Basic B1"** (~$13/month)
4. Click **"Review + create"** → **"Create"**

---

## Step 3: Create App Service (Web App)

1. Search for **"App Services"** in the top search bar
2. Click **"+ Create"** → **"Web App"**
3. Fill in **Basics** tab:
   - **Subscription**: Select your subscription
   - **Resource group**: `grace-rg`
   - **Name**: `grace-api` (this will be your URL: `grace-api.azurewebsites.net`)
   - **Publish**: `Code`
   - **Runtime stack**: `Java 21`
   - **Java web server stack**: `Java SE (Embedded Web Server)`
   - **Operating System**: `Linux`
   - **Region**: `East US`
   - **App Service Plan**: Select `grace-plan` (created in Step 2)
4. Click **"Review + create"** → **"Create"**
5. Wait for deployment to complete, then click **"Go to resource"**

---

## Step 4: Configure Environment Variables

1. In your App Service (`grace-api`), go to **Settings** → **Configuration**
2. Click **"+ New application setting"** and add each of these:

| Name | Value |
|------|-------|
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `AZURE_SQL_URL` | `jdbc:sqlserver://graceteam.database.windows.net:1433;database=grace_db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30` |
| `AZURE_SQL_USERNAME` | `graceteam` |
| `AZURE_SQL_PASSWORD` | `(your database password)` |
| `JWT_SECRET` | `(a random 32+ character string - generate one)` |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.azurestaticapps.net` |

3. Click **"Save"** at the top
4. Click **"Continue"** when prompted to restart the app

> 💡 **Tip**: To generate JWT_SECRET, you can use any random string generator or run `openssl rand -base64 32` in terminal.

---

## Step 5: Set Up GitHub Actions CI/CD

### 5.1 Create Service Principal for GitHub

1. Open [Azure Cloud Shell](https://shell.azure.com) (or use Azure CLI locally)
2. Run this command (replace `{subscription-id}` with your actual subscription ID):
   ```bash
   az ad sp create-for-rbac --name "grace-github-actions" --role contributor --scopes /subscriptions/{subscription-id}/resourceGroups/grace-rg --sdk-auth
   ```
3. Copy the **entire JSON output** - you'll need it for GitHub

> 💡 **Find your Subscription ID**: Azure Portal → Subscriptions → Copy the Subscription ID

### 5.2 Add Secret to GitHub Repository

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Fill in:
   - **Name**: `AZURE_CREDENTIALS`
   - **Secret**: Paste the JSON from step 5.1
5. Click **"Add secret"**

### 5.3 Verify Workflow Configuration

The workflow file `.github/workflows/azure-deploy.yml` is already created. Verify that `AZURE_WEBAPP_NAME` matches your App Service name:

```yaml
env:
  AZURE_WEBAPP_NAME: grace-api  # Must match your App Service name
```

---

## Step 6: Deploy

### Option A: Automatic Deployment (Recommended)

Push to `main` branch to trigger GitHub Actions:

```bash
git add .
git commit -m "Deploy to Azure"
git push origin main
```

Check deployment status at: **GitHub → Actions tab**

### Option B: Manual Deployment via Portal

1. Build the JAR locally:
   ```bash
   mvn clean package -DskipTests
   ```
2. In Azure Portal, go to your App Service → **Deployment Center**
3. Choose **Local Git** or **FTP** and upload the JAR file

---

## Step 7: Verify Deployment

1. Go to your App Service in Azure Portal
2. Click **"Browse"** (or open `https://grace-api.azurewebsites.net`)
3. Check the **Log stream**: App Service → **Monitoring** → **Log stream**

---

## Azure SQL Database Setup (If Not Done)

### Create SQL Server

1. Search for **"SQL servers"** in Azure Portal
2. Click **"+ Create"**
3. Fill in:
   - **Resource group**: `grace-rg`
   - **Server name**: `graceteam`
   - **Location**: `East US`
   - **Authentication**: `Use SQL authentication`
   - **Server admin login**: `graceteam`
   - **Password**: `(your strong password)`
4. Click **"Review + create"** → **"Create"**

### Create Database

1. Go to your SQL Server (`graceteam`)
2. Click **"+ Create database"**
3. Fill in:
   - **Database name**: `grace_db`
   - **Compute + storage**: Click **"Configure"** → Select **"Basic"** (~$5/month)
4. Click **"Review + create"** → **"Create"**

### Allow Azure Services Access

1. Go to your SQL Server (`graceteam`)
2. Click **Security** → **Networking**
3. Under **Firewall rules**, toggle **"Allow Azure services and resources to access this server"** to **Yes**
4. Click **"Save"**

---

## Troubleshooting

### View Logs
App Service → **Monitoring** → **Log stream**

### Check App Status
App Service → **Overview** → Check "Status" (should be "Running")

### Restart App
App Service → **Overview** → Click **"Restart"**

### Common Issues

| Issue | Solution |
|-------|----------|
| 502 Bad Gateway | Check Log stream for startup errors, verify Java version |
| Database connection failed | Verify AZURE_SQL_* settings, check SQL Server firewall |
| CORS errors | Check CORS_ALLOWED_ORIGINS matches your frontend URL exactly |
| JWT errors | Ensure JWT_SECRET is set and at least 32 characters |

